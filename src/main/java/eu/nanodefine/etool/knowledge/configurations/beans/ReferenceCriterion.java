/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations.beans;

import eu.nanodefine.etool.knowledge.ddos.Decidable;

/**
 * Represents a single criterion in the reference sheet.
 */
public class ReferenceCriterion implements Decidable {

	private String name;

	private String value;

	public ReferenceCriterion(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.name + " : " + this.value;
	}
}
