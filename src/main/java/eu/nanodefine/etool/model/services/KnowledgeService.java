/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.knowledge.configurations.ExplanationConfiguration;
import eu.nanodefine.etool.knowledge.configurations.MaterialConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PriorityConfiguration;
import eu.nanodefine.etool.knowledge.configurations.ReferenceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.TechniqueConfiguration;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.MaterialDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PriorityDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.ReferenceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.TechniqueDictionary;
import eu.nanodefine.etool.model.interfaces.IService;

/**
 * TODO refactor, this is a classic bad smell
 */
@Service
public class KnowledgeService implements IService {

	private static ExplanationConfiguration explanationConfiguration;

	private static ExplanationDictionary explanationDictionary;

	private static KnowledgeService instance;

	private static MaterialConfiguration materialConfiguration;

	private static MaterialDictionary materialDictionary;

	private static PerformanceConfiguration performanceConfiguration;

	private static PerformanceDictionary performanceDictionary;

	private static PriorityConfiguration priorityConfiguration;

	private static PriorityDictionary priorityDictionary;

	private static ReferenceConfiguration referenceConfiguration;

	private static ReferenceDictionary referenceDictionary;

	private static TechniqueConfiguration techniqueConfiguration;

	private static TechniqueDictionary techniqueDictionary;

	public KnowledgeService() {
		// Blargh
	}

	public static ExplanationConfiguration getExplanationConfiguration() {
		return explanationConfiguration;
	}

	@Autowired
	public void setExplanationConfiguration(
			ExplanationConfiguration explanationConfiguration) {
		KnowledgeService.explanationConfiguration = explanationConfiguration;
	}

	public static ExplanationDictionary getExplanationDictionary() {
		return explanationDictionary;
	}

	@Autowired
	public void setExplanationDictionary(
			ExplanationDictionary explanationDictionary) {
		KnowledgeService.explanationDictionary = explanationDictionary;
	}

	public static KnowledgeService getInstance() {
		if (instance == null) {
			instance = new KnowledgeService();
		}
		return instance;
	}

	public static MaterialConfiguration getMaterialConfiguration() {
		return materialConfiguration;
	}

	@Autowired
	public void setMaterialConfiguration(
			MaterialConfiguration materialConfiguration) {
		KnowledgeService.materialConfiguration = materialConfiguration;
	}

	public static MaterialDictionary getMaterialDictionary() {
		return materialDictionary;
	}

	@Autowired
	public void setMaterialDictionary(MaterialDictionary materialDictionary) {
		KnowledgeService.materialDictionary = materialDictionary;
	}

	public static PerformanceConfiguration getPerformanceConfiguration() {
		return performanceConfiguration;
	}

	@Autowired
	public void setPerformanceConfiguration(
			PerformanceConfiguration performanceConfiguration) {
		KnowledgeService.performanceConfiguration = performanceConfiguration;
	}

	public static PerformanceDictionary getPerformanceDictionary() {
		return performanceDictionary;
	}

	@Autowired
	public void setPerformanceDictionary(
			PerformanceDictionary performanceDictionary) {
		KnowledgeService.performanceDictionary = performanceDictionary;
	}

	public static PriorityConfiguration getPriorityConfiguration() {
		return priorityConfiguration;
	}

	@Autowired
	public void setPriorityConfiguration(
			PriorityConfiguration priorityConfiguration) {
		KnowledgeService.priorityConfiguration = priorityConfiguration;
	}

	public static PriorityDictionary getPriorityDictionary() {
		return priorityDictionary;
	}

	@Autowired
	public void setPriorityDictionary(PriorityDictionary priorityDictionary) {
		KnowledgeService.priorityDictionary = priorityDictionary;
	}

	public static ReferenceConfiguration getReferenceConfiguration() {
		return referenceConfiguration;
	}

	@Autowired
	public void setReferenceConfiguration(
			ReferenceConfiguration referenceConfiguration) {
		KnowledgeService.referenceConfiguration = referenceConfiguration;
	}

	public static ReferenceDictionary getReferenceDictionary() {
		return referenceDictionary;
	}

	@Autowired
	public void setReferenceDictionary(
			ReferenceDictionary referenceDictionary) {
		KnowledgeService.referenceDictionary = referenceDictionary;
	}

	public static TechniqueConfiguration getTechniqueConfiguration() {
		return techniqueConfiguration;
	}

	@Autowired
	public void setTechniqueConfiguration(
			TechniqueConfiguration techniqueConfiguration) {
		KnowledgeService.techniqueConfiguration = techniqueConfiguration;
	}

	public static TechniqueDictionary getTechniqueDictionary() {
		return techniqueDictionary;
	}

	@Autowired
	public void setTechniqueDictionary(
			TechniqueDictionary techniqueDictionary) {
		KnowledgeService.techniqueDictionary = techniqueDictionary;
	}

}
