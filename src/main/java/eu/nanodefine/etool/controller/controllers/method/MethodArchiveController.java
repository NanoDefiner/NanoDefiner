/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

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
 * Controller for method archival.
 */
@Controller
@RequestMapping("/" + Entities.METHOD + "/" + Actions.ARCHIVE)
public class MethodArchiveController extends AbstractMethodController {

	/**
	 * Archives several methods.
	 *
	 * <p>Works with an array of request parameters to determine the method IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String archiveMany(@RequestParam Integer[] methodIds, @RequestAttribute History history) {

		this.methodService.archiveUserMethodsByIds(this.getCurrentUser(), methodIds);

		return history.getRedirect();
	}

	/**
	 * Archives a single method.
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #archiveMany(Integer[], History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.METHOD + ".id={methodId}")
	@RequiresUser
	public String archiveOne(@PathVariable Integer methodId, @RequestAttribute History history) {
		return this.archiveMany(new Integer[] { methodId }, history);
	}
}
