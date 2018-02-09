/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.issue;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.Issue;
import eu.nanodefine.etool.model.repositories.IssueRepository;

/**
 * Controller for listing issues.
 */
@Controller
@RequestMapping(Entities.ISSUE + "/" + Actions.LIST)
public class IssueListController extends AbstractController {

	@Autowired
	private IssueRepository issueRepository;

	/**
	 * Lists all issues.
	 *
	 * <p>Issues are separated into three tables containing active, archived, and all issues.</p>
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String get(Model model) {

		Iterable<Issue> issues = this.issueRepository.findAll();
		List<Issue> issuesAll = new ArrayList<>();
		List<Issue> issuesActive = new ArrayList<>();
		List<Issue> issuesArchived = new ArrayList<>();

		// Separate active and archived issues
		for (Issue i : issues) {
			issuesAll.add(i);

			if (i.isArchived()) {
				issuesArchived.add(i);
			} else {
				issuesActive.add(i);
			}
		}

		// Add data to model
		model.addAttribute("issuesAll", issuesAll)
				.addAttribute("issuesActive", issuesActive)
				.addAttribute("issuesArchived", issuesArchived);

		return Templates.ISSUE_LIST;
	}

}
