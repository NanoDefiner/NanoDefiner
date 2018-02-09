/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import java.io.IOException;

import eu.nanodefine.etool.model.dto.Method;

/**
 * Processor for VSSA format analysis files.
 */
public class VSSAProcessor extends AbstractAnalysisProcessor {

	private static final String DMIN_VSSA = "dmin_vssa";

	private static final String D_VSSA = "d_vssa";

	private static final Integer NM_100 = 100;

	private static final Integer NM_1000 = 1000;

	private static final Integer NM_250 = 250;

	private static final String NM_UNIT = "nm";

	private static final String VSSA = "VSSA";

	private static final Integer VSSA_CUTOFF = 60;

	private static final String VSSA_UNIT = "m^2/cm^3";

	/**
	 * Number of small dimensions, <code>null</code> displays unknown.
	 */
	private Integer D;

	/**
	 * Presence of severe aggregation, <code>null</code> displays unknown.
	 */
	private Boolean aggregation;

	private String attrAggregation;

	private String attrDimensions;

	private String attrMultimodality;

	/**
	 * Diameter, µm-scaled
	 */
	private Double d_vssa;

	/**
	 * Diameter, nm-scaled
	 */
	private Double d_vssa_nm;

	/**
	 * Minimum diameter, µm-scaled.
	 */
	private Double dmin_vssa;

	/**
	 * Minimum diameter, nm-scaled.
	 */
	private Double dmin_vssa_nm;

	/**
	 * Presence of severe multimodality, <code>null</code> displays unknown.
	 */
	private Boolean multimodality;

	private Double vssa;

	public VSSAProcessor(Method method, String dimensions, String aggregation,
			String multimodality) throws IOException {
		super(method);
		this.vssa = this.vssa;
		this.attrDimensions = dimensions;
		this.attrAggregation = aggregation;
		this.attrMultimodality = multimodality;

		this.log.debug("vssa : " + this.vssa);
	}

	/**
	 * Determines the aggregation state.
	 */
	private void determineAggregation() {
		if ("?".equals(this.attrAggregation)) {
			this.aggregation = null; // Unknown
		} else if ("yes".equals(this.attrAggregation)) {
			this.aggregation = Boolean.TRUE; // Severe aggregation
		} else if ("no".equals(this.attrAggregation)) {
			this.aggregation = Boolean.FALSE; // No severe aggregation
		} else {
			this.aggregation = null; // Unknown (undefined)
		}

		this.log.debug("aggregation : " + this.aggregation);
	}

	/**
	 * Determines the number of small dimensions.
	 */
	private void determineD() {

		if ("?".equals(this.attrDimensions)) {
			this.D = null; // Unknown
		} else if ("{1d}".equals(this.attrDimensions)) {
			this.D = 1; // 1 small dimension
		} else if ("{2d}".equals(this.attrDimensions)) {
			this.D = 2; // 2 small dimension
		} else if ("{3d}".equals(this.attrDimensions)) {
			this.D = 3; // 3 small dimension
		} else if ("{mix}".equals(this.attrDimensions)) {
			this.D = null; // Unknown (mix)
		} else {
			this.D = null; // Unknown (undefined)
		}

		this.log.debug("D : " + this.D);
	}

	/**
	 * Determine VSSA value for dynamic number of small dimensions.
	 *
	 * TODO merge with {@link #determineDVSSA()}
	 */
	private void determineDMinVSSA() {
		if (this.D != null) {
			this.dmin_vssa = (2 * this.D) / this.vssa; // µm-scaled
			this.dmin_vssa_nm = Math.floor(this.dmin_vssa * 1000); // nm-scaled

			this.log.debug("dmin_vssa : " + this.dmin_vssa);
			this.log.debug("dmin_vssa_nm : " + this.dmin_vssa_nm);
		}
	}

	/**
	 * Determine VSSA value for D = 3.
	 */
	private void determineDVSSA() {
		this.d_vssa = 6 / this.vssa; // µm-scaled
		this.d_vssa_nm = Math.floor(this.d_vssa * 1000); // nm-scaled

		this.log.debug("d_vssa : " + this.d_vssa);
		this.log.debug("d_vssa_nm : " + this.d_vssa_nm);
	}

	/**
	 * Determines the multimodality of the material.
	 */
	private void determineMultimodality() {
		if ("?".equals(this.attrMultimodality)) {
			this.multimodality = null; // Unknown
		} else if ("yes".equals(this.attrMultimodality)) {
			this.multimodality = Boolean.TRUE; // Severe multimodality
		} else if ("no".equals(this.attrMultimodality)) {
			this.multimodality = Boolean.FALSE; // No severe multimodality
		} else {
			this.multimodality = null; // Unknown (undefined)
		}

		this.log.debug("multimodality : " + this.multimodality);
	}

	/**
	 * Processes the given VSSA value.
	 */
	@Override
	public void process(String vssa) throws IOException {

		this.vssa = Double.valueOf(vssa);

		// Determine all VSSA value related parts
		determineD();
		determineDVSSA();
		determineDMinVSSA();

		// Determine additional material criteria
		determineAggregation();
		determineMultimodality();

		this.result = this.d_vssa_nm;

		/*
		 * Directly identifiable as nanomaterial according to EC recommendation,
		 * not part of the screening strategy.
		 */
		if (this.d_vssa_nm < NM_100) {
			String argumentation = VSSA + " " + vssa + " > " + VSSA_CUTOFF
					+ VSSA_UNIT
					+ ", thus " + this.d_vssa_nm + NM_UNIT + " < " + NM_100
					+ NM_UNIT;
			//analysisResult.setArgumentation(argumentation);

			return;
		}

		/*
		 * Screening strategy step 1:
		 * TODO Documentation
		 */
		// Prelimiary porosity check, not processed here.

		/*
		 * Screening strategy step 2:
		 * TODO Documentation
		 */
		if (this.d_vssa_nm > NM_1000 && this.D == null) {
			/*analysisResult = new AnalysisResult();
			analysisResult.setAnalysisValue(d_vssa_nm);
			analysisResult.setAnalysisUnit(NM_UNIT);
			analysisResult.setNano(Boolean.FALSE);*/

			String argumentation = D_VSSA + " " + this.d_vssa_nm + NM_UNIT
					+ " > " + NM_1000 + NM_UNIT + " (D unknown, D=3 assumed)";

			//analysisResult.setArgumentation(argumentation);

			return;
		} else if (this.d_vssa_nm > NM_1000 && this.D != null) {
			this.result = this.dmin_vssa_nm;
			/*analysisResult = new AnalysisResult();
			analysisResult.setAnalysisValue(dmin_vssa_nm);
			analysisResult.setAnalysisUnit(NM_UNIT);
			analysisResult.setNano(Boolean.FALSE);*/

			String argumentation = DMIN_VSSA + " " + this.dmin_vssa_nm + NM_UNIT
					+ " > " + NM_1000 + NM_UNIT + " (D=" + this.D + ")";

			//analysisResult.setArgumentation(argumentation);

			return;
		}

		this.result = this.dmin_vssa_nm;

		/*
		 * Screening strategy step 3:
		 * TODO Documentation
		 *
		 * TODO Check: Aggregation?
		 * TODO Check: Multimodality?
		 */
		//if ((d_vssa_nm >= NM_100 && d_vssa_nm <= NM_1000) && D != null) {
		if (this.D != null) {

			// Substeps of step 3 following

			/*
			 * Screening strategy step 3d:
			 * TODO Documentation
			 */
			// TODO Multimodality check

			if (this.dmin_vssa_nm < NM_100) {

				/*
				 * Screening strategy step 3a:
				 * TODO Documentation
				 */

				/*analysisResult = new AnalysisResult();
				analysisResult.setAnalysisValue(dmin_vssa_nm);
				analysisResult.setAnalysisUnit(NM_UNIT);
				analysisResult.setNano(Boolean.TRUE);*/

				String argumentation = DMIN_VSSA + " " + this.dmin_vssa_nm + NM_UNIT
						+ " < " + NM_100 + NM_UNIT + " (D=" + this.D + ")";
				//analysisResult.setArgumentation(argumentation);

			} else if (this.dmin_vssa_nm > NM_250) {

				/*
				 * Screening strategy step 3b:
				 * TODO Documentation
				 */

				/*analysisResult = new AnalysisResult();
				analysisResult.setAnalysisValue(dmin_vssa_nm);
				analysisResult.setAnalysisUnit(NM_UNIT);
				analysisResult.setNano(Boolean.FALSE);*/

				String argumentation = DMIN_VSSA + " " + this.dmin_vssa_nm + NM_UNIT
						+ " > " + NM_250 + NM_UNIT + " (D=" + this.D + ")";
				//analysisResult.setArgumentation(argumentation);

			} else if (this.dmin_vssa_nm >= NM_100 && this.dmin_vssa_nm <= NM_250) {

				/*
				 * Screening strategy step 3c:
				 * TODO Documentation
				 */

				/*analysisResult = new AnalysisResult();
				analysisResult.setAnalysisValue(dmin_vssa_nm);
				analysisResult.setAnalysisUnit(NM_UNIT);
				analysisResult.setNano(null); // Borderline*/

				String argumentation = NM_100 + NM_UNIT + " <= " + DMIN_VSSA
						+ " " + this.dmin_vssa_nm + NM_UNIT + " <= " + NM_250
						+ NM_UNIT
						+ " (D=" + this.D + ")";
				//analysisResult.setArgumentation(argumentation);
			}

			return;
		}

		this.result = null;

		// Analysis result is null at this point? Oops...

		this.log.error("VSSA processing did not match any case");
		this.log.error("vssa : " + vssa);
		this.log.error("dimensions : " + this.attrDimensions);
		this.log.error("aggregation : " + this.attrAggregation);
		this.log.error("multimodality : " + this.attrMultimodality);

	}

}
