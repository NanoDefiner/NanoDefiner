/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.constants;

import org.springframework.stereotype.Component;

/**
 * Method attribute constants.
 */
@Component("MA")
public class MethodAttributes {

	/**
	 * Separator for components in multi-component values.
	 */
	public static final String COMPONENT_SEPARATOR = "|";

	/**
	 * Represents a failed size plausibility check.
	 */
	public static final String SIZE_PLAUSIBILITY_CHECK_FAILED = "size_plausibility_check_failed";

	/**
	 * Represents the need for a size plausibility check.
	 */
	public static final String SIZE_PLAUSIBILITY_CHECK_NEEDED = "size_plausibility_check_needed";

	/**
	 * Contains the size plausibility check value.
	 */
	public static final String SIZE_PLAUSIBILITY_CHECK_VALUE = "size_plausibility_check_value";

	/**
	 * Containts the SPCTv2 sheet list.
	 */
	public static final String SPCTv2_SHEET_LIST = "spctv2_sheet_list";

	/**
	 * Contains the chosen SPCTv2 sheet name.
	 */
	public static final String SPCTv2_SHEET_NAME = "spctv2_sheet_name";

}
