/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.nanodefine.etool.analysis.processors.VSSAProcessor;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.ArchivableService;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.view.report.DynamicPdfReport;

/**
 * Service for BET (VSSA) analysis result processing.
 */
@Service
public class VSSAService extends AbstractAnalysisDataFormatService {

	@Override
	public void addMethodResults(Method method, DynamicPdfReport report) throws IOException {

		report.addRow("VSSA value", method.getDataFile());

		if (method.hasResult()) {
			super.addMethodResults(method, report, this.determineDecisionKey(method), "VSSA");
		} else {
			super.addMethodResults(method, report, "none",
					this.ts.translate("method.update.error.bet"));
		}
	}

	@Override
	public Integer determineState(Method method) {
		// It is possible that uploaded data did not result in a decision or even in a D50
		if (method.getDataFile() != null) {
			return MethodStates.FINISHED;
		}

		return super.determineState(method);
	}

	@Override
	public String getAnalysisDataFormat() {
		return AnalysisDataFormats.VSSA;
	}

	@Override
	public void update(Method method, String data, Double uncertainty) throws Exception {
		super.update(method, data, uncertainty);

		MethodService ms = this.serviceManager.getBean(MethodService.class);
		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		// Prepare BET output
		// TODO multitype support
		Map<String, String> materialCriteriaMap =
				mcs.createMaterialCriteriaMap(this.serviceManager.getBean(ArchivableService.class)
						.getFirstNotArchived(method.getDossier().getMaterials()), false);

		String dimensions = materialCriteriaMap.get(PerformanceAttributes.DIMENSIONS);
		String aggregation = materialCriteriaMap.get(PerformanceAttributes.AGGREGATION);
		String multimodality = materialCriteriaMap.get(PerformanceAttributes.MULTIMODALITY);

		VSSAProcessor processor = new VSSAProcessor(method, dimensions,
				aggregation, multimodality);
		processor.process(data);
		// TODO save intermediate results

		ms.registerResult(method, processor.getResult());
	}
}
