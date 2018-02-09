/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.utilities.classes.UriBuilder;

/**
 * Controller for material creation.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.CREATE + "/"
		+ Entities.DOSSIER + ".id={dossierId}")
public class MaterialCreateController extends AbstractMaterialController {

	@Autowired
	private TranslationService translationService;

	/**
	 * Show the material creation form.
	 */
	@GetMapping(value = { "", "/" + Entities.MATERIAL + ".id={materialId}" })
	@RequiresUser
	public String get(Model model, @ModelAttribute Dossier dossier) {

		List<Material> materials = new ArrayList<>();

		if (dossier.getId() != 0) {
			this.validateUserAwareEntities(dossier);
			materials = this.materialTransactionalService.loadNotArchivedDossierMaterials(dossier);
		}

		this.addFormData(materials, new HashMap<>(), model);

		return Templates.MATERIAL_CREATE;
	}

	/**
	 * Persist a material or material template.
	 */
	@PostMapping
	@RequiresUser
	public String post(HttpServletRequest request, Model model,
			@ModelAttribute Dossier dossier, Material material,
			@RequestParam String submitForm) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		// Regular material criteria representation
		Set<MaterialCriterion> materialCriteria = mcs
				.createMaterialCriteriaFromRequestParameters(request.getParameterMap());

		// TODO Move to service
		material.setComment(request.getParameter(Entities.MATERIAL + ".comment"));

		if (dossier.getId() != 0) {
			this.validateUserAwareEntities(dossier);
			material.setDossier(dossier);
			material.setTemplate(false);
		} else {
			material.setTemplate(true);
		}

		material.setUser(this.getCurrentUser());
		material.setName(request.getParameter(Entities.MATERIAL + ".name"));
		material.setSignifier(request.getParameter(Entities.MATERIAL + "."
				+ PerformanceAttributes.MATERIAL_SIGNIFIER));

		material.setMaterialCriterions(materialCriteria);

		this.materialTransactionalService.persistMaterial(material, materialCriteria);

		UriBuilder redirect;

		// Build redirect
		if (dossier.getId() != 0) {
			// Dossier material: Either redirect to dossier overview or method creation
			if (submitForm.equals(Entities.METHOD)) {
				// Redirect to method creation
				redirect = this.uriService.builder(Entities.METHOD, Actions.CREATE).addEntityId(dossier)
						.addPathParam(Entities.METHOD + "." + PerformanceAttributes.TIER, "tier1");
			} else {
				// Redirect to materials tab
				redirect = this.uriService.builder(Entities.DOSSIER, Actions.READ)
						.addEntityId(dossier).setAnchor("materials");
			}
		} else {
			// Redirect to template material list
			redirect = this.uriService.builder(Entities.MATERIAL, Actions.LIST).setAnchor("templates");
		}

		return redirect.buildRedirect();
	}

}
