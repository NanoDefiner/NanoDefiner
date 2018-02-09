/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.services.dossier.DossierService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;
import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for the dashboard
 */
@Controller
public class DashboardController extends AbstractController {

	// TODO remove services
	@Autowired
	private DossierService dossierService;

	@Autowired
	private MaterialTransactionalService materialTransactionalService;

	@Autowired
	private MethodService methodService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserService userService;

	/**
	 * Add dossier actions to the model.
	 *
	 * <p>From the dashboard, new dossiers can be created.</p>
	 */
	@ModelAttribute
	public void dossierActionList(
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {
		String path = this.uriService.builder(Entities.DOSSIER, Actions.CREATE).build();

		actionList.add(new ActionListEntry(path, "dashboard.dossier_create"));
	}

	/**
	 * Display the dashboard.
	 */
	@RequiresUser
	@RequestMapping({ "/" + Entities.DASHBOARD,
			"/" + Entities.DASHBOARD + "/" + Actions.READ })
	public String view(Model model) {

		User user = this.getCurrentUser();

		// Load entities and add them to the model
		model.addAttribute("dossiers",
				this.dossierService.loadUserDossiers(user, false));

		model.addAttribute("materials",
				this.materialTransactionalService.loadNotArchivedUserMaterials(user));

		model.addAttribute("methods",
				this.methodService.loadNotArchivedUserMethods(user));

		model.addAttribute("reports",
				this.reportService.loadNotArchivedUserReports(user));

		return Templates.DASHBOARD;
	}
}
