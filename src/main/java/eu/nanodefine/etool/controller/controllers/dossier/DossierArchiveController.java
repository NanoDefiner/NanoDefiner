/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.dossier;

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
 * Controller for dossier archival.
 *
 * TODO validate permissions
 */
@Controller
@RequestMapping("/" + Entities.DOSSIER + "/" + Actions.ARCHIVE)
public class DossierArchiveController extends AbstractDossierController {

	/**
	 * Archives several dossiers.
	 *
	 * <p>Works with an array of request parameters to determine the dossier IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String archiveMany(@RequestParam Integer[] dossierIds,
			@RequestAttribute History history) {

		this.dossierService.archiveUserDossiersByIds(this.getCurrentUser(), dossierIds);

		return history.getRedirect();
	}

	/**
	 * Archives a single dossier.
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #archiveMany(Integer[], History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.DOSSIER + ".id={dossierId}")
	@RequiresUser
	public String archiveOne(@PathVariable Integer dossierId,
			@RequestAttribute History history) {

		// TODO merge the two methods into one
		return this.archiveMany(new Integer[] { dossierId }, history);
	}
}
