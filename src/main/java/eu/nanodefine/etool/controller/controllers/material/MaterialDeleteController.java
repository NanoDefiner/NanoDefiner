/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;

/**
 * Controller for material template removal.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.DELETE)
public class MaterialDeleteController extends AbstractMaterialController {

	/**
	 * Delete several material templates.
	 *
	 * <p>Works with an array of request parameters to determine the material IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String deleteMany(@RequestParam Integer[] materialIds, @RequestAttribute History history) {

		this.serviceManager.getBean(MaterialTransactionalService.class)
				.deleteUserTemplatesByIds(this.getCurrentUser(), materialIds);

		return this.uriService.builder(Entities.MATERIAL, Actions.LIST).setAnchor("templates")
				.buildRedirect();
	}

	/**
	 * Delete a single material template.
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #deleteMany(Integer[], History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.MATERIAL + ".id={materialId}")
	@RequiresUser
	public String deleteOne(@PathVariable Integer materialId, @RequestAttribute History history) {

		return this.deleteMany(new Integer[] { materialId }, history);
	}

}
