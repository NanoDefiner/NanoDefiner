/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import eu.nanodefine.etool.knowledge.configurations.beans.PerformanceCriterion;
import eu.nanodefine.etool.knowledge.configurations.beans.PriorityCriterion;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.model.services.KnowledgeService;

/**
 * Simple implementation of a {@link DroolsDecisionObject} just for
 * discrimination purpose in Drools rules. Just usable for
 * {@link PerformanceCriterion} objects.
 */
public class PerformanceCriterionDDO extends DroolsDecisionObject {

	private String techniqueSignifier;

	private String materialSignifier;

	private Boolean match;

	private String matchReason;

	private Double priority;

	public PerformanceCriterionDDO(PerformanceCriterion performanceCrit,
			String techniqueSignifier) {
		this(performanceCrit, techniqueSignifier, "default");
	}

	public PerformanceCriterionDDO(PerformanceCriterion performanceCrit,
			String techniqueSignifier, String materialSignifier) {
		super(performanceCrit);

		this.techniqueSignifier = techniqueSignifier;
		this.materialSignifier = materialSignifier;

		PriorityCriterion priorityCrit = KnowledgeService
				.getPriorityConfiguration()
				.getEntry(techniqueSignifier, materialSignifier)
				.getEntry(performanceCrit.getName());

		if (!techniqueSignifier.equals(priorityCrit.getValue())
				&& !materialSignifier.equals(priorityCrit.getValue())) {
			this.priority = Double.valueOf(priorityCrit.getValue());
		}

		this.match = Boolean.FALSE;
		this.matchReason = ExplanationDictionary.MISMATCH;
	}

	public Boolean getMatch() {
		return this.match;
	}

	public void setMatch(Boolean match) {
		this.match = match;
	}

	public String getMatchReason() {
		return this.matchReason;
	}

	public void setMatchReason(String matchReason) {
		this.matchReason = matchReason;

		// TODO put this somewhere else?
		this.setMatch(!matchReason.equals(ExplanationDictionary.MISMATCH));
	}

	public String getTechniqueSignifier() {
		return this.techniqueSignifier;
	}

	public String getMaterialSignifier() {
		return this.materialSignifier;
	}

	public Double getPriority() {
		return this.priority;
	}

	@Override
	public String toString() {
		return getName() + " : " + getValue();
	}

}
