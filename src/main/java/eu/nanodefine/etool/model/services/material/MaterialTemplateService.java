/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.knowledge.configurations.MaterialConfiguration;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.services.view.TemplateService;

/**
 * Template service for material-related processing
 */
@Service("materialTS")
public class MaterialTemplateService extends TemplateService {

	private final MaterialConfiguration materialConfiguration;

	@Autowired
	public MaterialTemplateService(ServiceManager serviceManager,
			MaterialConfiguration materialConfiguration) {
		super(serviceManager);
		this.materialConfiguration = materialConfiguration;
	}

	/**
	 * Determines the column background color depending on the match status.
	 */
	public String determineAttributeColumnBackground(String matchStatus) {
		switch (matchStatus) {
			case ExplanationDictionary.MATCH:
				return "success";
			case ExplanationDictionary.MISMATCH:
				return "danger";
			case ExplanationDictionary.INCOMPLETENESS_MATERIAL:
			case ExplanationDictionary.INCOMPLETENESS_TECHNIQUE:
				return "warning";
			default:
				return "faded";
		}
	}

	/**
	 * Determines the glyphicon depending on the match status.
	 */
	public String determineGlyphicon(String matchStatus) {
		switch (matchStatus) {
			case ExplanationDictionary.MATCH:
				return "ok-sign";
			case ExplanationDictionary.MISMATCH:
				return "remove-sign";
			case ExplanationDictionary.INCOMPLETENESS_MATERIAL:
			case ExplanationDictionary.INCOMPLETENESS_TECHNIQUE:
				return "question-sign";
			default:
				return "none";
		}
	}

	/**
	 * Determines the CSS class depending on the incompleteness value.
	 */
	public String determineIncompletenessClass(Double incompleteness) {
		return incompleteness > .5 ? "danger" : (incompleteness > .2 ? "warning" : "success");
	}

	/**
	 * Determines the material signifier string, i.e. the material group/type.
	 *
	 * <p>This is used to display the material group/type after the material name. If no specific
	 * material group/type was chosen (i.e. "default" material group), an empty string is returned.
	 * </p>
	 */
	public String determineMaterialSignifierString(String materialSignifier) {
		if (materialSignifier.equals(PerformanceDictionary.DEFAULT_MATERIAL_SIGNIFIER)) {
			return "";
		}

		return " (" + this.materialConfiguration.getEntry(materialSignifier).getName()  + ")";
	}
}
