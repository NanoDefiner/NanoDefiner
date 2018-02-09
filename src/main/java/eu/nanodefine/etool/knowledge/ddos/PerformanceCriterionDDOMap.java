/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.beans.PerformanceCriterion;

/**
 * Simple {@link Map} implementation just for discrimination purpose in Drools
 * rules. Just usable for {@link PerformanceCriterionDDO} objects.
 */
public class PerformanceCriterionDDOMap
		extends HashMap<String, PerformanceCriterionDDO>
		implements Map<String, PerformanceCriterionDDO> {

	private static final long serialVersionUID = -4908126732109213252L;

	private Logger log = LoggerFactory
			.getLogger(PerformanceCriterionDDOMap.class);

	private Boolean match;

	private String materialSignifier;

	private String techniqueSignifier;

	/**
	 * Conversion constructor.
	 *
	 * @param map A regular {@link PerformanceCriterion} map.
	 */
	public PerformanceCriterionDDOMap(Map<String, PerformanceCriterion> map) {

		this.techniqueSignifier = map.get(
				PerformanceAttributes.TECHNIQUE_SIGNIFIER).getValue();
		this.materialSignifier = map.get(
				PerformanceAttributes.MATERIAL_SIGNIFIER).getValue();

		for (String key : map.keySet()) {
			put(key, new PerformanceCriterionDDO(map.get(key),
					this.techniqueSignifier, this.materialSignifier));
		}
		this.match = Boolean.FALSE;
	}

	/**
	 * Conversion constructor.
	 *
	 * @param set A regular {@link PerformanceCriterion} set.
	 */
	public PerformanceCriterionDDOMap(Set<PerformanceCriterion> set) {
		String nameDummy;

		for (PerformanceCriterion crit : set) {
			nameDummy = crit.getName();
			if (PerformanceAttributes.TECHNIQUE_SIGNIFIER.equals(nameDummy)) {
				this.techniqueSignifier = crit.getValue();
			}
			if (PerformanceAttributes.MATERIAL_SIGNIFIER.equals(nameDummy)) {
				this.materialSignifier = crit.getValue();
			}
		}

		for (PerformanceCriterion crit : set) {
			nameDummy = crit.getName();
			put(nameDummy, new PerformanceCriterionDDO(crit,
					this.techniqueSignifier, this.materialSignifier));
		}
		this.match = Boolean.FALSE;
	}

	public Boolean getMatch() {
		return this.match;
	}

	public void setMatch(Boolean match) {
		this.match = match;
	}

	public String getMaterialSignifier() {
		return this.materialSignifier;
	}

	public String getTechniqueSignifier() {
		return this.techniqueSignifier;
	}

	@Override
	public String toString() {
		String s = "performance map: (" + this.techniqueSignifier + ","
				+ this.materialSignifier + ")";
		for (PerformanceCriterionDDO ddo : this.values()) {
			s += "\n-> " + ddo;
		}
		return s;

	}
}
