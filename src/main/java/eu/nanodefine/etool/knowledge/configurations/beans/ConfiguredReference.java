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
 * Represents an entry of the reference sheet.
 *
 * TODO Refactor, unnecessary complex structure
 */
public class ConfiguredReference {

	private Set<ReferenceCriterion> entries;

	public ConfiguredReference() {
		this.entries = new HashSet<>();
	}

	public Boolean add(ReferenceCriterion referenceCriterion) {
		return this.entries.add(referenceCriterion);
	}

	public Set<ReferenceCriterion> getEntries() {
		return this.entries;
	}

	public ReferenceCriterion getEntry(String name) {
		Objects.requireNonNull(name);
		for (ReferenceCriterion c : this.entries) {
			if (name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}

}
