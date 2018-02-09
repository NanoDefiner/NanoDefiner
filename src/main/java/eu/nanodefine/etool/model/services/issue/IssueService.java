/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.issue;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.google.common.collect.ImmutableMap;

import eu.nanodefine.etool.model.dto.Issue;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.IssueRepository;
import eu.nanodefine.etool.model.services.MailService;
import eu.nanodefine.etool.security.NanoDefinerRealm;

/**
 * Service for issue-related processing.
 */
@Service
public class IssueService implements IService {

	@Value("${nanodefiner.error.issue_autocreate}")
	private boolean autocreateIssues;

	@Autowired
	private IssueRepository issueRepository;

	private Logger log = LoggerFactory.getLogger(IssueService.class);

	@Autowired
	private NanoDefinerRealm securityRealm;

	@Autowired
	private ServiceManager serviceManager;

	/**
	 * Archives the issues with the given IDs.
	 */
	@Transactional
	public void archiveIssuesByIds(Integer[] issueIds) {
		Iterable<Issue> issues = this.issueRepository
				.findAll(Arrays.asList(issueIds));

		issues.forEach(i -> i.setArchived(true));
	}

	/**
	 * Creates an automatic issue from the given error attributes if it is enabled in the config.
	 */
	public Optional<Issue> createAutomaticIssue(Map<String, Object> errorAttributesMap) {
		// Abort if automatic issue creation is disabled
		if (!this.autocreateIssues) {
			return Optional.empty();
		}

		String text = errorAttributesMap.get("exception").toString();

		return Optional.of(this.createIssue(errorAttributesMap, text));
	}

	/**
	 * Creates an issue from the given error attributes and text.
	 */
	public Issue createIssue(Map<String, Object> errorAttributesMap,
			String text) {

		StringBuilder textBuilder = new StringBuilder(text);

		if (errorAttributesMap == null) {
			errorAttributesMap = ImmutableMap.of();
		}

		// Add user information
		User user = this.securityRealm.getUser();
		textBuilder.append("\n\nUser: ").append(user.getId() != null ? user.getUsername() : "Guest")
				.append("\n");

		// Add error details if any
		if (!errorAttributesMap.isEmpty()) {
			textBuilder.append("\n\nError details:\n\n");

			for (Map.Entry<String, Object> entry : errorAttributesMap.entrySet()) {
				textBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString())
						.append("\n");
			}
		}

		// Create and persist issue
		Issue issue = new Issue(user.getId() != null ? user : null, textBuilder.toString());

		Issue issuePersisted = this.issueRepository.save(issue);

		this.log.debug("Created issue: {}", issue.getUser() != null ?
				issue.getUser().getUsername() : "Unknown");

		// Prepare and send mail
		Model model = new ExtendedModelMap();
		model.addAttribute("feedback",
				issue.getText().replaceAll("\n", "<br />"));

		String subject = errorAttributesMap.isEmpty() ?
				"User feedback" : "Problem report";

		this.serviceManager.getBean(MailService.class).sendAdminMail("mail/feedback", subject, model);

		return issuePersisted;
	}

	/**
	 * Updates the given issue.
	 *
	 * <p>Only the comment can be changed, so the issue will be loaded, the comment will changed and
	 * the issue will be persisted again.</p>
	 */
	public Issue updateIssue(Issue issue, String comment) {

		Issue issuePersisted = this.issueRepository.findOne(issue.getId());

		issuePersisted.setComment(comment);
		issuePersisted.setArchived(false);

		// TODO This is not necessary... right?
		return this.issueRepository.save(issuePersisted);
	}

}
