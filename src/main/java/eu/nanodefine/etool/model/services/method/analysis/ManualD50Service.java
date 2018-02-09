/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import org.springframework.stereotype.Service;

import eu.nanodefine.etool.analysis.processors.D50Processor;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;

/**
 * Service for manual D_50 analysis result processing.
 */
@Service
public class ManualD50Service extends AbstractAnalysisDataFormatService {

	@Override
	public String getAnalysisDataFormat() {
		return AnalysisDataFormats.MANUAL_D50;
	}

	@Override
	public void update(Method method, String data, Double uncertainty) throws Exception {
		super.update(method, data, uncertainty);

		MethodService ms = this.serviceManager.getBean(MethodService.class);

		D50Processor processor = new D50Processor(method);
		processor.process(data);

		ms.registerResult(method, processor.getResult());
	}
}
