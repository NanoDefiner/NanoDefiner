/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.constants;

import org.springframework.stereotype.Component;

/**
 * Numeric method state constants.
 */
@Component
public class MethodStates {

	/**
	 * Analysis data submitted but no results yet.
	 */
	public static int ANALYSIS_DATA = 10;

	/**
	 * No analysis data submitted yet.
	 */
	public static int CREATED = 0;

	/**
	 * Method has been finished.
	 */
	public static int FINISHED = 30;

	/**
	 * Results are available but further processing is needed.
	 */
	public static int INTERMEDIATE_RESULTS = 20;

	/**
	 * Analysis data submitted but plausibility check needed.
	 */
	public static int SIZE_PLAUSIBILITY_CHECK = 21;

	/**
	 * User needs to select sheet for SPCTv2 method.
	 */
	public static int SPCTv2_SHEET_SELECTION = 11;

	/**
	 * Error state.
	 */
	public static int UNKNOWN = -1;
}
