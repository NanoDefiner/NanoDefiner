/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations.beans;

/**
 * Represents an entry of the material sheet.
 */
public class ConfiguredMaterial {

	private String comment;

	private String group;

	private String materialSignifier;

	private String name;

	public ConfiguredMaterial() {

	}

	public ConfiguredMaterial(String materialSignifier, String group, String name,
			String comment) {
		super();
		this.materialSignifier = materialSignifier;
		this.group = group;
		this.name = name;
		this.comment = comment;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getMaterialSignifier() {
		return this.materialSignifier;
	}

	public void setMaterialSignifier(String signifier) {
		this.materialSignifier = signifier;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "material_signifier: " + this.materialSignifier
				+ "; group: " + this.group
				+ "; name: " + this.name
				+ "; comment: " + this.comment;
	}

}
