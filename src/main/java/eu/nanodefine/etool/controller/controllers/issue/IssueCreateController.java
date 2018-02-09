/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.issue;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.dto.Issue;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.services.issue.IssueService;

/**
 * Controller for issue creation.
 */
@Controller
@RequestMapping(Entities.ISSUE + "/" + Actions.CREATE)
public class IssueCreateController extends AbstractController {

	/**
	 * Persist an issue.
	 *
	 * <p>Requests to this method are coming from the feedback form included on every page.</p>
	 */
	@PostMapping
	public String post(@RequestAttribute History history,
			@SessionAttribute(name = "errorAttributesMap", required = false)
					Map<String, Object> errorAttributesMap,
			@RequestParam("feedback") String feedback,
			@RequestAttribute("successes") List<String> successes) {

		User user = this.getCurrentUser();

		Issue issue = this.serviceManager.getBean(IssueService.class)
				.createIssue(errorAttributesMap, feedback);

		// Clear error attributes
		if (errorAttributesMap != null) {
			errorAttributesMap.clear();
		}

		successes.add("global.feedback.success");

		return history.getRedirect();
	}

}
