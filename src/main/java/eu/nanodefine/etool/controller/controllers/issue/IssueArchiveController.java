/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.issue;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.services.issue.IssueService;

/**
 * Controller for issue archival.
 */
@Controller
@RequestMapping(Entities.ISSUE + "/" + Actions.ARCHIVE)
public class IssueArchiveController extends AbstractController {

	/**
	 * Archives one or several issues based on request parameters.
	 *
	 * <p>Since issues can only be archived from the issue table, we don't need a method to archive
	 * a single issue.</p>
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String archiveMany(@RequestParam Integer[] issueIds,
			@RequestAttribute History history) {

		this.serviceManager.getBean(IssueService.class)
				.archiveIssuesByIds(issueIds);

		// TODO can't we just redirect to the issue table, where else could the user be coming from?
		return history.getRedirect();
	}

}
