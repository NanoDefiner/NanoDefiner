/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for material overview.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.READ + "/"
		+ Entities.MATERIAL + ".id={materialId}")
public class MaterialReadController extends AbstractMaterialController {

	/**
	 * Create material / material template archive / delete action list.
	 *
	 * TODO move to service
	 */
	private List<ActionListEntry> archiveDeleteActionList(Dossier dossier,
			Material material, boolean template) {

		// No actions if the dossier is archived
		if (dossier.isArchived()) {
			return ImmutableList.of();
		}

		// Archive or delete action
		if (!template) {
			// Dossier materials can be archived
			String archive = this.uriService
					.builder(Entities.MATERIAL, Actions.ARCHIVE)
					.addEntityId(material).build();

			return ImmutableList.of(new ActionListEntry(archive,
					"material.read.material_archive", "archive",
					!material.isArchived()));
		} else {
			// Material templates can be deleted
			String redirect = this.uriService.builder(Entities.MATERIAL, Actions.LIST)
					.setAnchor("templates").build();
			String delete = this.uriService.builder(Entities.MATERIAL, Actions.DELETE)
					.addRedirectParam(redirect)
					.addQueryParam("materialIds", material.getId()).build();

			return ImmutableList.of(new ActionListEntry(delete,
					"material.read.material_delete", "delete"));
		}
	}

	/**
	 * Show material overview.
	 */
	@GetMapping
	@RequiresUser
	public String get(Model model, @ModelAttribute Material material) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		this.validateUserAwareEntities(material);

		model.addAttribute("materialCriteria", mcs.createMaterialCriteriaMap(material, true));

		return Templates.MATERIAL_READ;

	}

	/**
	 * Create material action list.
	 *
	 * <p>Available actions are material update and material template creation.</p>
	 *
	 * TODO move to service, remove model attribute
	 */
	@ModelAttribute
	public void materialActionList(@ModelAttribute Material material,
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {

		Dossier dossier = material.getDossier();
		boolean template = false;

		// For templates
		if (dossier == null) {
			dossier = new Dossier();
			dossier.setId(0);
			template = true;
		}

		String update = this.uriService.builder(Entities.MATERIAL, Actions.UPDATE)
				.addEntityId(material).build();

		// Updating is only available if the dossier is not archived
		if (!dossier.isArchived()) {
			actionList.add(new ActionListEntry(update,
					"material.read.material_update", "update",
					!material.isArchived()));
		}

		// Add archive / delete actions
		actionList.addAll(this.archiveDeleteActionList(dossier, material, template));

		// Add custom attribute actions
		actionList.addAll(this.customAttributeActionList(material));

		// Create template action, always available
		String createTemplate = this.uriService.builder(Entities.MATERIAL, Actions.CREATE)
				.addPathParam(Entities.DOSSIER + ".id", "0").addEntityId(material).build();
		actionList.add(new ActionListEntry(createTemplate, "material.read.material_create"));
	}

}
