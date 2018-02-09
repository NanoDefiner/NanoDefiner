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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.analysis.processors.CMFProcessor;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.view.PlotService;
import eu.nanodefine.etool.view.report.DynamicPdfReport;

/**
 * Service for manual particle sizer (Q0) analysis result processing.
 */
@Service
public class ParticleSizerService extends AbstractAnalysisDataFormatService {

	private Logger log = LoggerFactory.getLogger(ParticleSizerService.class);

	@Override
	public void addMethodResults(Method method, DynamicPdfReport report) throws IOException {

		this.addAnalysisFile(method, report);

		super.addMethodResults(method, report);

		this.addPlots(method, report);
	}

	@Override
	public String getAnalysisDataFormat() {
		return AnalysisDataFormats.PARTICLE_SIZER;
	}

	@Override
	public void update(Method method, String data, Double uncertainty) throws Exception {

		MethodService ms = this.serviceManager.getBean(MethodService.class);

		CMFProcessor processor = new CMFProcessor(method, this.fileService);
		try {
			super.update(method, data, uncertainty);
			processor.process(data);

			String dataPath = this.fileService.getFullPathInAnalysisDirectory(data).toString();

			// Create plots
			PlotService ps = this.serviceManager.getBean(PlotService.class);
			Path filePathDistribution = Paths.get(dataPath + ".distribution.png");
			Path filePathDensity = Paths.get(dataPath + ".density.png");

			Files.write(filePathDistribution, ps.createCMFDistributionPlot(processor),
					StandardOpenOption.CREATE_NEW);

			Files.write(filePathDensity, ps.createCMFDensityPlot(processor),
					StandardOpenOption.CREATE_NEW);

			ms.registerResult(method, processor.getResult());
		} catch (IOException e) {
			// TODO re-throw custom exception?
			this.log.error("Exception during plot creation:", e);
		}
	}
}
