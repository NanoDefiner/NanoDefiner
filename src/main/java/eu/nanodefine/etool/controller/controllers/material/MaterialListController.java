/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

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
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for material lists.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.LIST)
public class MaterialListController extends AbstractMaterialController {

	/**
	 * Display material list.
	 *
	 * <p>Materials are separated into active, archived and all dossier materials, as well as
	 * material templates.</p>
	 */
	@GetMapping
	@RequiresUser
	public String get(Model model) {

		List<Material> materials = this.materialTransactionalService
				.loadUserMaterials(this.getCurrentUser());
		List<Material> materialsAll = new ArrayList<>(),
				materialsActive = new ArrayList<>(),
				materialsArchived = new ArrayList<>(),
				materialsTemplate = new ArrayList<>();

		// Separate materials
		for (Material m : materials) {
			if (m.isTemplate()) {
				materialsTemplate.add(m);
			} else {
				materialsAll.add(m);

				if (m.isArchived() || m.getDossier().isArchived()) {
					materialsArchived.add(m);
				} else {
					materialsActive.add(m);
				}
			}
		}

		// Add data to model
		model.addAttribute("materialsAll", materialsAll)
				.addAttribute("materialsActive", materialsActive)
				.addAttribute("materialsArchived", materialsArchived)
				.addAttribute("materialsTemplate", materialsTemplate);

		return Templates.MATERIAL_LIST;
	}

	/**
	 * Adds an action that allows creation of material templates.
	 */
	@ModelAttribute
	public void templateActionList(
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {
		String path = this.uriService.builder(Entities.MATERIAL, Actions.CREATE)
				.addPathParam(Entities.DOSSIER + ".id", "0").build();

		actionList.add(new ActionListEntry(path, "material.list.template_create"));
	}

}
