/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.advice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller advice which makes configuration variables available as model attributes.
 *
 * TODO remove properties only need in templates and replace them with
 *
 * @environment.getProperty('config.property')
 */
@ControllerAdvice
public class ConfigAdvice {

	/**
	 * Whether to show the feedback button.
	 */
	@Value("${dev.feedback}")
	private boolean feedback;

	/**
	 * Absolute link to the full manual.
	 */
	@Value("${knowledge.manual}")
	private String manualLink;

	/**
	 * Production mode (false implies debug mode).
	 */
	@Value("${dev.production}")
	private boolean production;

	/**
	 * Whether activation mails will be sent to users.
	 */
	@Value("${mail.user.activation}")
	private boolean sendActivationMail;

	/**
	 * Whether admins should be notified of new users.
	 */
	@Value("${mail.admin.user_created}")
	private boolean sendAdminMailUserCreated;

	/**
	 * Make debug state available as model attribute.
	 */
	@ModelAttribute("debugMode")
	public boolean debugMode() {
		return !this.production;
	}

	/**
	 * Make feedback configuration available as model attribute.
	 */
	@ModelAttribute("feedbackButtonEnabled")
	public boolean feedback() {
		return this.feedback;
	}

	/**
	 * Make manual link available as model attribute.
	 */
	@ModelAttribute("manualLink")
	public String manualLink() {
		return this.manualLink;
	}

	/**
	 * Make activation mail setting available as model attribute.
	 */
	@ModelAttribute("sendActivationMail")
	public boolean sendActivationMail() {
		return this.sendActivationMail;
	}

	/**
	 * Make admin mail on new users setting available as model attribute.
	 */
	@ModelAttribute("sendAdminMailUserCreated")
	public boolean sendAdminMailUserCreated() {
		return this.sendAdminMailUserCreated;
	}
}
