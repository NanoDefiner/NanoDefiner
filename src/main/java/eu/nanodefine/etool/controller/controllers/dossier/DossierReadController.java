/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.dossier;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for viewing dossiers.
 */
@Controller
@RequestMapping("/" + Entities.DOSSIER + "/" + Actions.READ + "/"
		+ Entities.DOSSIER + ".id={dossierId}")
public class DossierReadController extends AbstractDossierController {

	/**
	 * Displays dossier information.
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Dossier dossier,
			@ModelAttribute("actionList") List<ActionListEntry> actionList,
			@RequestAttribute History history, Model model) {

		MaterialTransactionalService materialTransactionalService
				= this.serviceManager.getBean(MaterialTransactionalService.class);
		MethodService methodService = this.serviceManager.getBean(MethodService.class);
		ReportService reportService = this.serviceManager.getBean(ReportService.class);

		this.validateUserAwareEntities("Invalid dossier ID or dossier access denied", dossier);

		// Load materials, methods, and reports
		List<Material> materials =
				materialTransactionalService.loadNotArchivedDossierMaterials(dossier);
		List<Method> methods = methodService.loadNotArchivedDossierMethods(dossier);
		List<Report> reports = reportService.loadNotArchivedDossierReports(dossier);

		// Determine method result status
		boolean hasMethodsWithResults = methodService.hasMethodsWithDataFile(dossier);
		boolean hasNonBorderlineMethodResult = methodService.hasNonBorderlineResult(dossier);

		// Whether there's an active report
		boolean reportDone = !this.serviceManager.getBean(ReportService.class)
				.loadNotArchivedDossierReports(dossier).isEmpty();

		// Material status
		boolean hasMaterials = !materials.isEmpty();
		// Whether materials can be created
		boolean canCreateMaterials = dossier.isMulticonstituent() || materials.isEmpty();

		// Add data to model
		model.addAttribute("materials", materials)
				.addAttribute("methods", methods)
				.addAttribute("reports", reports);

		model.addAttribute("hasMethodsWithResults", hasMethodsWithResults);

		// Abstract workflow information
		model.addAttribute("recommendedAction",
				this.dossierService.determineRecommendedAction(dossier));

		model.addAttribute("materialReady", true); // TODO remove if it's always true?
		model.addAttribute("materialDone", !materials.isEmpty());
		model.addAttribute("materialHref", dossier.isArchived() || hasMaterials
				? "#materials" : this.uriService.buildMaterialCreationUri(dossier).build());

		model.addAttribute("methodReady", !materials.isEmpty());
		model.addAttribute("methodDone", hasMethodsWithResults);
		// Link to methods tab if dossier is archived or if there are methods without results or if
		// no materials have been created yet; otherwise link to method creation
		model.addAttribute("methodHref", dossier.isArchived() ||
				methodService.hasNotArchivedMethodsWithoutResults(dossier) || !hasMaterials ? "#methods" :
				this.uriService.buildMethodCreationUri(dossier, methods.isEmpty()
						? "tier1" : "tier2").build());

		model.addAttribute("reportReady", hasMethodsWithResults);
		model.addAttribute("reportDone", reportDone);
		model.addAttribute("reportHref", dossier.isArchived() || reportDone || !hasMethodsWithResults
				? "#reports" : this.uriService.buildReportCreationUri(dossier).build());

		// Add actions only if the dossier is not archived
		if (!dossier.isArchived()) {
			actionList.addAll(this.dossierActionList(dossier, history));

			actionList.addAll(this.customAttributeActionList(dossier));

			actionList.addAll(this.materialActionList(dossier, canCreateMaterials));

			actionList.addAll(this.serviceManager.getBean(MethodService.class)
					.buildMethodCreateActionList(dossier, hasMaterials));

			actionList.addAll(this.reportActionList(dossier, hasMethodsWithResults));
		}

		return Templates.DOSSIER_READ;
	}

	/**
	 * Create material action list.
	 *
	 * <p>Adds an action for material creation.</p>
	 *
	 * TODO move to service
	 */
	private List<ActionListEntry> materialActionList(Dossier dossier,
			boolean enabled) {
		String path = this.uriService.builder(Entities.MATERIAL, Actions.CREATE)
				.addEntityId(dossier).build();

		return ImmutableList.of(new ActionListEntry(path,
				"dossier.read.material_create", enabled));
	}

	/**
	 * Create report action list.
	 *
	 * <p>Adds an action for report creation.</p>
	 *
	 * TODO move to service
	 */
	private List<ActionListEntry> reportActionList(Dossier dossier,
			boolean enabled) {
		String path = this.uriService.builder(Entities.REPORT, Actions.CREATE)
				.addEntityId(dossier).build();

		return ImmutableList.of(new ActionListEntry(path,
				"dossier.read.report_create", enabled));
	}

}
