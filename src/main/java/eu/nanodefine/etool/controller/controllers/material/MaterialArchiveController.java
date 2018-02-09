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

/**
 * Controller for material archival.
 *
 * TODO validate permissions
 *
 * TODO merge methods into one
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/" + Actions.ARCHIVE)
public class MaterialArchiveController extends AbstractMaterialController {

	/**
	 * Archives several materials.
	 *
	 * <p>Works with an array of request parameters to determine the material IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String archiveMany(@RequestParam Integer[] materialIds,
			@RequestAttribute History history) {

		this.materialTransactionalService.archiveUserMaterialsByIds(this.getCurrentUser(), materialIds);

		return history.getRedirect();
	}

	/**
	 * Archive a single material.
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #archiveMany(Integer[], History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.MATERIAL + ".id={materialId}")
	@RequiresUser
	public String archiveOne(@PathVariable Integer materialId, @RequestAttribute History history) {

		return this.archiveMany(new Integer[] { materialId }, history);
	}

}
