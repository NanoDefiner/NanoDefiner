/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.material;

import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDO;

/**
 * Helper class to combine compatibility information between material and technique.
 */
public class MaterialCriterionTechniqueCompatibility {

	private String explanation;

	private MaterialCriterionDDO mddo;

	private PerformanceCriterionDDO pddo;

	public MaterialCriterionTechniqueCompatibility(
			MaterialCriterionDDO mddo,
			PerformanceCriterionDDO pddo,
			String explanation) {
		this.mddo = mddo;
		this.pddo = pddo;
		this.explanation = explanation;
	}

	/**
	 * Returns the explanation.
	 *
	 * @see eu.nanodefine.etool.knowledge.configurations.ExplanationConfiguration
	 */
	public String getExplanation() {
		return this.explanation;
	}

	/**
	 * Returns the match reason.
	 *
	 * @see PerformanceCriterionDDO#getMatchReason()
	 */
	public String getMatchReason() {
		return this.pddo.getMatchReason();
	}

	/**
	 * Returns the material criterion.
	 */
	public MaterialCriterionDDO getMaterialCriterion() {
		return this.mddo;
	}

	/**
	 * Returns the performance criterion.
	 */
	public PerformanceCriterionDDO getPerformanceCriterion() {
		return this.pddo;
	}
}
