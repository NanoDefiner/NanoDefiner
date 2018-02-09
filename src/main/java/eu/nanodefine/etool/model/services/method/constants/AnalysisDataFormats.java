/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.constants;

import org.springframework.stereotype.Component;

/**
 * Analysis data format constants.
 */
@Component("ADF")
public class AnalysisDataFormats {

	/**
	 * Manual D50 entry.
	 */
	public static final String MANUAL_D50 = "D50"; // TODO change value

	/**
	 * Q0 distribution.
	 *
	 * TODO check naming
	 */
	public static final String PARTICLE_SIZER = "q0"; // TODO change value

	/**
	 * RIKILT Single Particle Calculation Tool version 2.
	 */
	public static final String RIKILT_SPCTv2 = "RIKILT_SPCTv2";

	public static final String UNKNOWN = "UNKNOWN";

	/**
	 * VSSA value for BET.
	 */
	public static final String VSSA = "VSSA";
}
