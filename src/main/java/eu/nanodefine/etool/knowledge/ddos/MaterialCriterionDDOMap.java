/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.nanodefine.etool.model.dto.MaterialCriterion;

/**
 * Simple {@link Map} implementation just for discrimination purpose in Drools
 * rules. Just usable for {@link MaterialCriterionDDO} objects.
 */
public class MaterialCriterionDDOMap
		extends HashMap<String, MaterialCriterionDDO>
		implements Map<String, MaterialCriterionDDO> {

	private static final long serialVersionUID = 8819671840079656870L;

	/**
	 * Conversion constructor.
	 *
	 * @param map A regular {@link MaterialCriterion} map.
	 */
	public MaterialCriterionDDOMap(
			Map<String, MaterialCriterion> map) {
		for (String key : map.keySet()) {
			put(key, new MaterialCriterionDDO(map.get(key)));
		}
	}

	/**
	 * Conversion constructor.
	 *
	 * @param set A regular {@link MaterialCriterion} set.
	 */
	public MaterialCriterionDDOMap(Set<MaterialCriterion> set) {
		for (MaterialCriterion criterion : set) {
			put(criterion.getName(), new MaterialCriterionDDO(criterion));
		}
	}

}
