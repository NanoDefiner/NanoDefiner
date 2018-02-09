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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.services.report.ReportService;

/**
 * Controller for updating dossiers.
 */
@Controller
@RequestMapping("/" + Entities.DOSSIER + "/" + Actions.UPDATE + "/"
		+ Entities.DOSSIER + ".id={dossierId}")
public class DossierUpdateController extends AbstractDossierController {

	/**
	 * Display dossier creation form pre-filled with the dossier values.
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute("dossierForm") Dossier dossier, Model model) {
		this.validateUserAwareEntities(dossier);

		return Templates.DOSSIER_CREATE;
	}

	/**
	 * Persist dossier changes.
	 */
	@PostMapping
	@RequiresUser
	public String post(@PathVariable Integer dossierId, @ModelAttribute Dossier dossierRequest,
			@ModelAttribute("dossierForm") @Valid Dossier dossierForm, BindingResult result) {

		this.validateUserAwareEntities(dossierRequest);

		// TODO restrict possible parameters for the dossier
		// TODO proper error page and messages
		if (dossierRequest.getId() == null || result.hasErrors()) {
			return Templates.ERROR_GENERIC;
		}

		dossierForm.setId(dossierRequest.getId());

		Dossier dossierPersisted = this.dossierService.updateDossier(dossierForm);

		// Archive reports
		this.serviceManager.getBean(ReportService.class).archiveDossierReports(dossierPersisted);

		// Redirect to materials tab if no material created, methods otherwise
		// TODO do we need to load it from the DB? can't we just use dossierRequest.getMaterials()?
		String tab = this.dossierRepository.findOne(dossierForm.getId()).getMaterials()
				.isEmpty() ? "materials" : "methods";

		// Redirect to dossier view page
		return this.uriService.builder(Entities.DOSSIER, Actions.READ)
				.addPathParam(Entities.DOSSIER + ".id", dossierId).setAnchor(tab)
				.buildRedirect();
	}
}
