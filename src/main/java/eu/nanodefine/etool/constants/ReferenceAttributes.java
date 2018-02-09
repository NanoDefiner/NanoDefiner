/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.constants;

import org.springframework.stereotype.Component;

/**
 * Constant collection for reference attributes.
 *
 * TODO move redundant attributes from PerformanceAttributes and ReferenceAttributes into separate
 * class or just make one class for both?
 */
@Component("RA")
public class ReferenceAttributes {

	/*
	 * TODO Criterion category
	 */
	public static final String REFERENCE_SIGNIFIER = "reference_signifier";

	public static final String REFERENCE_NAME = "reference_name";

	public static final String REFERENCE_COMMENT = "reference_comment";

	/*
	 * TODO Criterion category
	 */
	public static final String TRADE_FORM = "trade_form";

	/*
	 * TODO Criterion category
	 */
	public static final String DISPERSIBILITY = "dispersibility";

	public static final String CHEMICAL_COMPOSITION = "chemical_composition";

	public static final String COMPOSITES = "composites";

	public static final String DIMENSIONS = "dimensions";

	public static final String SHAPE = "shape";

	public static final String ANALYSIS_TEMPERATURE = "analysis_temperature";

	public static final String ELECTRON_BEAM = "electron_beam";

	public static final String POLYDISPERSITY = "polydispersity";

	public static final String MULTIMODALITY = "multimodality";

	public static final String CONDUCTIVITY = "conductivity";

	public static final String MAGNETISM = "magnetism";

	public static final String FUNCTIONALISATION = "functionalisation";

	public static final String LIGHT_ABSORPTION = "light_absorption";

	public static final String FLUORESCENCE = "fluorescence";

	public static final String AGGREGATION = "aggregation";

	public static final String AGGLOMERATION = "agglomeration";

	public static final String RELEASE_IAM = "release_iam";

	/*
	 * TODO Criterion category
	 */
	public static final String WR_SIZE_RANGE = "wr_size_range";

	public static final String PRESENCE = "presence";

	public static final String OS_VACUUM = "os_vacuum";

}
