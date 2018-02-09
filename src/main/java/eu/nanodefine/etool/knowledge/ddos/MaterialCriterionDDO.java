/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import eu.nanodefine.etool.model.dto.MaterialCriterion;

/**
 * Simple implementation of a {@link DroolsDecisionObject} just for
 * discrimination purpose in Drools rules. Just usable for
 * {@link MaterialCriterion} objects.
 */
public class MaterialCriterionDDO extends DroolsDecisionObject {

	public MaterialCriterionDDO(MaterialCriterion materialCriterion) {
		super(materialCriterion);
	}

}
