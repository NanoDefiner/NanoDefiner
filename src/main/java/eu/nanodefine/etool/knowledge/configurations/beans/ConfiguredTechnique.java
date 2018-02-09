/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations.beans;

/**
 * Simple representation class for technique configuration entries.
 */
public class ConfiguredTechnique {

	private String signifier;

	private String name;

	private String comment;

	public ConfiguredTechnique() {

	}

	public ConfiguredTechnique(String signifier, String name, String comment) {
		super();
		this.signifier = signifier;
		this.name = name;
		this.comment = comment;
	}

	public String getSignifier() {
		return this.signifier;
	}

	public void setSignifier(String signifier) {
		this.signifier = signifier;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "signifier: " + this.signifier
				+ "; name: " + this.name
				+ "; comment: " + this.comment;
	}

}
