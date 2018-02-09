/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations.beans;

/**
 * Represents an entry of the explanation sheet.
 */
public class ConfiguredExplanation {

	private String attribute;

	private String match;

	private String mismatch;

	private String incompletenessMaterial;

	private String incompletenessTechnique;

	private String irrelevance;

	public ConfiguredExplanation() {

	}

	public ConfiguredExplanation(String attribute, String match,
			String mismatch, String incompletenessMaterial,
			String incompletenessTechnique, String irrelevance) {
		super();
		this.attribute = attribute;
		this.match = match;
		this.mismatch = mismatch;
		this.incompletenessMaterial = incompletenessMaterial;
		this.incompletenessTechnique = incompletenessTechnique;
		this.irrelevance = irrelevance;
	}

	public String getAttribute() {
		return this.attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getMatch() {
		return this.match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getMismatch() {
		return this.mismatch;
	}

	public void setMismatch(String mismatch) {
		this.mismatch = mismatch;
	}

	public String getIncompletenessMaterial() {
		return this.incompletenessMaterial;
	}

	public void setIncompletenessMaterial(String incompletenessMaterial) {
		this.incompletenessMaterial = incompletenessMaterial;
	}

	public String getIncompletenessTechnique() {
		return this.incompletenessTechnique;
	}

	public void setIncompletenessTechnique(String incompletenessTechnique) {
		this.incompletenessTechnique = incompletenessTechnique;
	}

	public String getIrrelevance() {
		return this.irrelevance;
	}

	public void setIrrelevance(String irrelevance) {
		this.irrelevance = irrelevance;
	}

	@Override
	public String toString() {
		return "attribute: " + this.attribute
				+ "; match: " + this.match
				+ "; mismatch: " + this.mismatch
				+ "; incompleteness_material: " + this.incompletenessMaterial
				+ "; incompleteness_technique: " + this.incompletenessTechnique
				+ "; irrelevance: " + this.irrelevance;
	}

}
