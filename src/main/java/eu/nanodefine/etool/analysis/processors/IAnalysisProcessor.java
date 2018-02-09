/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import eu.nanodefine.etool.model.dto.Method;

/**
 * Analysis processor interface.
 */
public interface IAnalysisProcessor {

	/**
	 * Returns the method of this processor.
	 */
	public Method getMethod();

	/**
	 * Returns D_50 analysis result.
	 */
	public Double getResult();

	/**
	 * Prcoesses the given analysis data.
	 */
	public void process(String analysisData) throws Exception;

}
