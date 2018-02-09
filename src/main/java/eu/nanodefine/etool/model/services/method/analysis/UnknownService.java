/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import org.springframework.stereotype.Service;

import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;

/**
 * Dummy service for the "unknown" analysis data format.
 */
@Service
public class UnknownService extends AbstractAnalysisDataFormatService {

	@Override
	public String getAnalysisDataFormat() {
		return AnalysisDataFormats.UNKNOWN;
	}
}
