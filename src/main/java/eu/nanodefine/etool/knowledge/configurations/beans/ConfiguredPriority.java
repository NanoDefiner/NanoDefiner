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
 * Represents an entry of the priority sheet.
 *
 * TODO Refactor, unnecessary complex structure
 */
public class ConfiguredPriority {

	private Set<PriorityCriterion> entries;

	public ConfiguredPriority() {
		this.entries = new HashSet<>();
	}

	public Boolean add(PriorityCriterion performanceCriterion) {
		return this.entries.add(performanceCriterion);
	}

	public Set<PriorityCriterion> getEntries() {
		return this.entries;
	}

	public PriorityCriterion getEntry(String name) {
		Objects.requireNonNull(name);
		for (PriorityCriterion c : this.entries) {
			if (name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}

}
