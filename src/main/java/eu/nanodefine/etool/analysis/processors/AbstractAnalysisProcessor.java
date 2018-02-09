/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.nanodefine.etool.model.dto.Method;

/**
 * Common logic for analysis processors.
 */
public abstract class AbstractAnalysisProcessor implements IAnalysisProcessor {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	protected Double result = null;

	private Method method;

	AbstractAnalysisProcessor(Method method) {
		this.method = method;
	}

	@Override
	public Method getMethod() {
		return this.method;
	}

	@Override
	public Double getResult() {
		return this.result;
	}

}
