/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.model.services.material.MaterialService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.technique.TechniqueService;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;

/**
 * Controller for method creation.
 */
@Controller
@RequestMapping("/" + Entities.METHOD + "/" + Actions.CREATE + "/"
		+ Entities.DOSSIER + ".id={dossierId}/"
		+ Entities.METHOD + "." + PerformanceAttributes.TIER + "={tier}")
public class MethodCreateController extends AbstractMethodController {

	/**
	 * Persist method.
	 */
	@PostMapping
	@RequiresUser
	public String createPost(@ModelAttribute("methodForm") @Valid Method methodForm,
			BindingResult result, @ModelAttribute Dossier dossier) {

		this.validateUserAwareEntities(dossier);

		// TODO validate form inputs
		Method methodPersisted = this.methodService.persistMethod(dossier, methodForm);

		// Redirect to analysis tab of the method overview
		return this.uriService.builder(Entities.METHOD, Actions.READ)
				.addEntityId(methodPersisted).setAnchor("analysis").buildRedirect();
	}

	/**
	 * Display the method creation form.
	 *
	 * <p>The user gets to choose a technique, preparation protocol / comment, as well as name and
	 * comment for the method.</p>
	 *
	 * @see AbstractMethodController#techniques(Dossier, Optional, Method)
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Dossier dossier, Model model, @PathVariable String tier,
			@ModelAttribute("techniquesMap") Map<Technique, String> techniqueMap) {

		this.validateUserAwareEntities(dossier);

		TechniqueService ts = this.serviceManager.getBean(TechniqueService.class);
		MaterialService ms = this.serviceManager.getBean(MaterialService.class);
		MaterialTransactionalService mts
				= this.serviceManager.getBean(MaterialTransactionalService.class);
		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		// TODO solve cleaner, perform technique map creation here and directly get a list that way?
		List<Technique> techniques = new ArrayList<>(techniqueMap.keySet());

		// Collect relevant data
		List<Material> materials = mts.loadNotArchivedDossierMaterials(dossier);

		Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>
				compatibilityTableMap = mcs.createMaterialCriteriaTechniqueCompatibilityTableMap(materials);

		Table<String, Attribute, List<Material>> aggregatedTechniqueMaterialWarningsTable =
				ts.createAggregatedTechniqueMaterialWarnings(techniques, materials);

		// Add data to model
		model.addAttribute("tier", tier);
		model.addAttribute("tierSwitch", this.methodService.determineTierSwitch(tier));

		model.addAttribute("materialCriteriaTechniqueCompatibilityTableMap", compatibilityTableMap);

		model.addAttribute("techniqueIncompletenessMap", ts.createTechniqueIncompletenessMap());

		model.addAttribute("techniqueGeneralWarningsMap",
				ts.createTechniqueGeneralWarningsMap(techniques));

		model.addAttribute("techniqueMaterialWarningsTable",
				aggregatedTechniqueMaterialWarningsTable);

		model.addAttribute("materialIncompletenessTable",
				ms.createMaterialIncompletenessTable(materials));

		return Templates.METHOD_CREATE;
	}
}
