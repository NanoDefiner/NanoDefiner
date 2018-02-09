/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.issue;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.Issue;
import eu.nanodefine.etool.model.services.issue.IssueService;

/**
 * Controller for updating issues.
 */
@Controller
@RequestMapping(Entities.ISSUE + "/" + Actions.UPDATE + "/"
		+ Entities.ISSUE + ".id={issueId}")
public class IssueUpdateController extends AbstractController {

	/**
	 * Creates a redirect to the issue list.
	 *
	 * TODO move to abstract issue controller and use in issue creation as well
	 */
	private String buildIssueListRedirect() {
		return this.uriService.builder(Entities.ISSUE, Actions.LIST).buildRedirect();
	}

	/**
	 * Validates the given issue.
	 *
	 * TODO move to service or validation bean?
	 */
	private void checkIssue(Issue issue, List<String> errors) {
		// Invalid / missing issue ID
		if (issue.getId() == 0) {
			errors.add("issue.error.id");
		}
	}

	/**
	 * Displays the issue update form.
	 *
	 * <p>The admins can add or change comments for the issue on this page.</p>
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String get(@ModelAttribute Issue issue,
			@RequestAttribute("errors") List<String> errors) {

		this.checkIssue(issue, errors);

		// Redirect back to issue overview on invalid / missing issue ID
		if (!errors.isEmpty()) {
			return this.buildIssueListRedirect();
		}

		return Templates.ISSUE_UPDATE;
	}

	/**
	 * Persists issue changes.
	 *
	 * <p>Only the comment of an issue can be changed.</p>
	 */
	@PostMapping
	@RequiresRoles({ "admin" })
	public String post(@ModelAttribute Issue issue,
			@RequestParam(Entities.ISSUE + ".comment") String comment,
			@RequestAttribute("errors") List<String> errors,
			@RequestAttribute("successes") List<String> successes) {

		this.checkIssue(issue, errors);

		// Redirect back to issue overview on invalid / missing issue ID
		if (!errors.isEmpty()) {
			return this.buildIssueListRedirect();
		}

		this.serviceManager.getBean(IssueService.class).updateIssue(issue, comment);

		successes.add("issue.success.update");

		return this.buildIssueListRedirect();
	}

}
