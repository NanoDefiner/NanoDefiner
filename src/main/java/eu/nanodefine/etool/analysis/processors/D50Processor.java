/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import eu.nanodefine.etool.model.dto.Method;

/**
 * Processor for manual D50 values.
 */
public class D50Processor extends AbstractAnalysisProcessor {

	public D50Processor(Method method) {
		super(method);
	}

	@Override
	public void process(String analysisData) {
		// TODO better error handling
		this.result = Double.valueOf(analysisData);
	}
}
