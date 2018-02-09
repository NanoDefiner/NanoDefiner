/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations.beans;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an entry of the performance sheet.
 *
 * TODO Refactor, unnecessary complex structure
 */
public class ConfiguredPerformance {

	private Set<PerformanceCriterion> entries;

	public ConfiguredPerformance() {
		this.entries = new HashSet<>();
	}

	public Boolean add(PerformanceCriterion performanceCriterion) {
		return this.entries.add(performanceCriterion);
	}

	public Set<PerformanceCriterion> getEntries() {
		return this.entries;
	}

	public PerformanceCriterion getEntry(String name) {
		Objects.requireNonNull(name);
		for (PerformanceCriterion c : this.entries) {
			if (name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}

}
