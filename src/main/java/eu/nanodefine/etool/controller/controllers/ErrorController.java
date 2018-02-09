/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.services.ErrorService;
import eu.nanodefine.etool.model.services.issue.IssueService;
import eu.nanodefine.etool.security.NanoDefinerRealm;

/**
 * Handles errors in the user interaction.
 *
 * TODO what about errors happening during AJAX requests and similar?
 */
@Controller
@SessionAttributes("errorAttributesMap")
public class ErrorController extends AbstractController
		implements org.springframework.boot.autoconfigure.web.ErrorController {

	private static final String PATH = "/" + Entities.ERROR;

	@Autowired
	private ErrorAttributes errorAttributes;

	@Autowired
	private NanoDefinerRealm securityRealm;

	/**
	 * Generic error page.
	 *
	 * TODO why does the user suddenly seem unauthenticated?
	 */
	@RequestMapping({ "/" + Entities.ERROR })
	public String error(HttpServletRequest request,
			HttpServletResponse response, Model model) {

		Optional<Session> session = Optional.ofNullable(this.securityRealm.getSession());

		Map<String, Object> errorAttributesMap = this.serviceManager.getBean(ErrorService.class)
				.prepareGenericErrorPage(request, response, this.errorAttributes, session, model);

		switch (response.getStatus()) {
			case 404:
				return this.error404(errorAttributesMap, model);
			//case 500:
			//return this.error500(errorAttributesMap, model);
			default:
				return this.errorGeneric(errorAttributesMap, model);
		}
	}

	/**
	 * Handles 404 (not found) errors.
	 */
	private String error404(Map<String, Object> errorAttributesMap, Model model) {
		return Templates.ERROR_GENERIC;
	}

	/**
	 * Handles 500 (internal server error) errors.
	 *
	 * <p>Currently unused.</p>
	 */
	private String error500(Map<String, Object> errorAttributesMap, Model model) {
		return Templates.ERROR_GENERIC;
	}

	/**
	 * Handles generic and other errors.
	 */
	private String errorGeneric(Map<String, Object> errorAttributesMap,
			Model model) {

		this.serviceManager.getBean(IssueService.class)
				.createAutomaticIssue(errorAttributesMap);

		return Templates.ERROR_GENERIC;
	}

	/**
	 * Error path as required by the
	 * {@link org.springframework.boot.autoconfigure.web.ErrorController}.
	 */
	@Override
	public String getErrorPath() {
		return ErrorController.PATH;
	}
}
