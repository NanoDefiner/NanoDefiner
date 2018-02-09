/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.dossier;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;

/**
 * Controller for dossier creation.
 */
@Controller
@RequestMapping(Entities.DOSSIER + "/" + Actions.CREATE)
public class DossierCreateController extends AbstractDossierController {

	/**
	 * Display dossier creation form.
	 */
	@GetMapping
	@RequiresUser
	public String get(Dossier dossierForm, Model model) {

		// Set default values
		dossierForm.setPurpose("{reach}");
		dossierForm.setMulticonstituent(false);

		model.addAttribute("dossierForm", dossierForm);

		return Templates.DOSSIER_CREATE;
	}

	/**
	 * Persist dossier.
	 */
	@PostMapping
	@RequiresUser
	public String post(@ModelAttribute("dossierForm") @Valid Dossier dossierForm,
			BindingResult result) {
		// TODO restrict parameters that can be passed from the form and add proper
		// validation

		// Display form again if there were errors
		if (result.hasErrors()) {
			return Templates.DOSSIER_CREATE;
		}

		Dossier dossier = this.dossierService.persistDossier(dossierForm, this.getCurrentUser());

		// Redirect to material creation
		return this.uriService.builder(Entities.MATERIAL, Actions.CREATE).addEntityId(dossier)
				.buildRedirect();
	}

}
