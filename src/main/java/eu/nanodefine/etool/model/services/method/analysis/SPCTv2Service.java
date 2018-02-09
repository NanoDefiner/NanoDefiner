/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.analysis.processors.SPCTv2Processor;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.method.constants.MethodAttributes;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.model.services.view.PlotService;
import eu.nanodefine.etool.view.report.DynamicPdfReport;

/**
 * Service for Single Particle Calculation Tool version 2 (SPCTv2) analysis result processing.
 *
 * @see <a href="https://www.wur.nl/en/show/Single-Particle-Calculation-tool.htm">wur.nl</a>
 */
@Service
public class SPCTv2Service extends AbstractAnalysisDataFormatService {

	private Logger log = LoggerFactory.getLogger(SPCTv2Service.class);

	@Override
	public void addMethodResults(Method method, DynamicPdfReport report) throws IOException {

		this.addAnalysisFile(method, report);
		// Add information about the chosen sheet
		report.addRow("Sheet", method.getAttribute(MethodAttributes.SPCTv2_SHEET_NAME).getValue());

		super.addMethodResults(method, report);

		this.addPlots(method, report);
	}

	/**
	 * Creates method results and plots.
	 */
	private void createResults(Method method, SPCTv2Processor processor)
			throws Exception {

		MethodService ms = this.serviceManager.getBean(MethodService.class);

		ms.registerResult(method, processor.getResult());
		method.addAttribute(MethodAttributes.SPCTv2_SHEET_NAME, processor.getSheet().getSheetName());

		String fileName = ms.getMethodDataFilePath(method).toString();

		PlotService ps = this.serviceManager.getBean(PlotService.class);

		// Create plots
		try {
			Path filePathDistribution = Paths.get(fileName + ".distribution.png");
			Path filePathDensity = Paths.get(fileName + ".density.png");

			Files.write(filePathDistribution, ps.createSPICPMSDistributionPlot(processor),
					StandardOpenOption.CREATE_NEW);

			Files.write(filePathDensity, ps.createSPICPMSDensityPlot(processor),
					StandardOpenOption.CREATE_NEW);

		} catch (IOException e) {
			// TODO re-throw custom exception?
			this.log.error("Exception during plot creation:", e);
		}
	}

	@Override
	public Integer determineState(Method method) {
		return method.hasAttribute(MethodAttributes.SPCTv2_SHEET_LIST)
				? MethodStates.SPCTv2_SHEET_SELECTION : super.determineState(method);
	}

	@Override
	public String getAnalysisDataFormat() {
		return AnalysisDataFormats.RIKILT_SPCTv2;
	}

	/**
	 * Returns a list of sheet names for the given method.
	 *
	 * <p>If the method has no sheet list, an empty list is returned.</p>
	 */
	public List<String> getSheetList(Method method) {
		if (!method.hasAttribute(MethodAttributes.SPCTv2_SHEET_LIST)) {
			return ImmutableList.of();
		}

		return Arrays.asList(method.getAttribute(MethodAttributes.SPCTv2_SHEET_LIST).getValue()
				.split(Pattern.quote(MethodAttributes.COMPONENT_SEPARATOR)));
	}

	/**
	 * Processes further analysis data, which in this case can only be a sheet number.
	 */
	@Override
	public void update(Method method, String data) throws Exception {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		// TODO handle errors
		if (method.hasAttribute(MethodAttributes.SPCTv2_SHEET_LIST)) {
			SPCTv2Processor processor = new SPCTv2Processor(method, this.fileService);
			Integer sheetIndex = Integer.valueOf(data);
			processor.process(ms.getMethodDataFilePath(method).toString(), sheetIndex);

			this.createResults(method, processor);

			method.removeAttribute(MethodAttributes.SPCTv2_SHEET_LIST);
		}

		super.update(method, data);
	}

	/**
	 * Processes initial method analysis data.
	 *
	 * <p>If the uploaded analysis file contains more than one sheet (besides the calibration sheet),
	 * adjust the method state so that the user has to pick a sheet. Otherwise, process analysis data
	 * as usual.</p>
	 */
	@Override
	public void update(Method method, String data, Double uncertainty) throws Exception {

		super.update(method, data, uncertainty);
		SPCTv2Processor processor = new SPCTv2Processor(method, this.fileService);
		processor.process(data);

		// If no result could be found, assume we have more than one sheet in the result file
		// TODO differentiate between invalid file and sheet selection?
		if (processor.getResult() == null) {
			StringBuilder sheets = new StringBuilder("");

			// TODO we assume that indices start at 1, calibration sheet is 0
			for (String sheetName : processor.getSheetMap().values()) {
				sheets.append("|").append(sheetName);
			}

			method.addAttribute(MethodAttributes.SPCTv2_SHEET_LIST, sheets.toString().substring(1));
		} else {
			// Only one sheet in the file, results can be created now
			this.createResults(method, processor);
		}
	}
}
