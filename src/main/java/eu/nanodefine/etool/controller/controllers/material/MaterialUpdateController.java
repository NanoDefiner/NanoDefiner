/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;

/**
 * Controller for updating materials.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.UPDATE + "/"
		+ Entities.MATERIAL + ".id={materialId}")
public class MaterialUpdateController extends AbstractMaterialController {

	/**
	 * Displays the material creation form.
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Material material, Model model) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		this.validateUserAwareEntities(material);

		this.addFormData(
				this.materialTransactionalService.loadOtherNotArchivedDossierMaterials(material),
				mcs.createMaterialCriteriaMap(material, false), model);

		return Templates.MATERIAL_CREATE;
	}

	/**
	 * Persists material changes.
	 */
	@PostMapping
	@RequiresUser
	public String post(@ModelAttribute Material material,
			@ModelAttribute Dossier dossier, HttpServletRequest request) {

		this.validateUserAwareEntities(dossier, material);

		this.materialTransactionalService.updateMaterialFromRequestParameters(material,
				request.getParameterMap());

		String redirect;

		// Redirect depending on whether it's a dossier material or material template
		if (dossier.getId() != 0) {
			redirect = this.uriService.builder(Entities.DOSSIER, Actions.READ)
					.addEntityId(dossier).setAnchor("materials").buildRedirect();
		} else {
			redirect = this.uriService.builder(Entities.MATERIAL, Actions.LIST)
					.setAnchor("templates").buildRedirect();
		}

		// TODO add success message?
		return redirect;
	}

}
