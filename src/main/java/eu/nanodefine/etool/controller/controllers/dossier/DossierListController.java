/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.dossier;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for listing dossiers.
 */
@Controller
@RequestMapping("/" + Entities.DOSSIER + "/" + Actions.LIST)
public class DossierListController extends AbstractDossierController {

	/**
	 * Creates the dossier action list.
	 *
	 * <p>From the dossier list, just like from the dashboard, new dossiers can be created.</p>
	 *
	 * TODO Move to service
	 */
	@ModelAttribute
	public void dossierActionList(
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {
		String path = this.uriService.builder(Entities.DOSSIER, Actions.CREATE)
				.build();

		actionList.add(new ActionListEntry(path, "dashboard.dossier_create"));
	}

	/**
	 * Displays a list of the user's dossiers.
	 *
	 * <p>Separate tables for active and archived dossiers are created.</p>
	 */
	@RequiresUser
	@GetMapping
	public String get(Model model) {

		User user = this.getCurrentUser();

		// Get user's dossiers and store them in request
		List<Dossier> dossiers = this.dossierService.loadUserDossiers(user);
		List<Dossier> dossiersActive = new ArrayList<>(dossiers.size());
		List<Dossier> dossiersArchived = new ArrayList<>(dossiers.size());

		// Separate active and archived dossiers
		for (Dossier d : dossiers) {
			if (d.isArchived()) {
				dossiersArchived.add(d);
			} else {
				dossiersActive.add(d);
			}
		}

		// Add data to model
		model.addAttribute("dossiersAll", dossiers);
		model.addAttribute("dossiersActive", dossiersActive);
		model.addAttribute("dossiersArchived", dossiersArchived);

		return Templates.DOSSIER_LIST;
	}
}
