/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.dictionaries.beans;

/**
 * Represents an option of a performance / material criterion.
 *
 * <p>An option consists of a name and a label.</p>
 */
public class Option {

	public static final String KEY_VALUE_SEPARATOR = "=";

	public static final String OPTION_SEPARATOR = "\n";

	private String label = null;

	private String name = null;

	public Option() {
		super();
	}

	public Option(String key, String label) {
		super();
		this.name = key;
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
