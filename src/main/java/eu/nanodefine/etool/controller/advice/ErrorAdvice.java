/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.advice;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.ShiroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.exception.runtime.InvalidRequestParametersException;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.services.ErrorService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.model.services.view.UriService;
import eu.nanodefine.etool.security.NanoDefinerRealm;

/**
 * General exception handling across all controllers.
 *
 * This is necessary because exception-handling is done for each controller
 * individually – exceptions thrown in actions of a controller can only be
 * caught by an exception handler in that same controller.
 */
@ControllerAdvice
public class ErrorAdvice {

	private final ErrorAttributes errorAttributes;

	private final NanoDefinerRealm securityRealm;

	private final ServiceManager serviceManager;

	private final TranslationService translationService;

	private Logger log = LoggerFactory.getLogger(ErrorAdvice.class);

	@Value("${spring.http.multipart.max-file-size}")
	private String maxFileSize;

	@Value("${spring.http.multipart.max-request-size}")
	private String maxRequestSize;

	@Autowired
	public ErrorAdvice(ServiceManager serviceManager,
			TranslationService translationService, NanoDefinerRealm securityRealm,
			ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
		this.securityRealm = securityRealm;
		this.serviceManager = serviceManager;
		this.translationService = translationService;
	}

	/**
	 * Handles invalid request parameters.
	 */
	@ExceptionHandler(InvalidRequestParametersException.class)
	public String invalidRequestParameter(InvalidRequestParametersException ex, Model model,
			HttpServletRequest request, HttpServletResponse response) {
		this.log.error("Invalid request parameters: {}", ex.getMessage());

		response.setStatus(HttpStatus.BAD_REQUEST.value());

		this.serviceManager.getBean(ErrorService.class).prepareGenericErrorPage(request, response,
				this.errorAttributes, Optional.ofNullable(this.securityRealm.getSession()), model);

		return Templates.ERROR_GENERIC;
	}

	/**
	 * Handles Shiro authentication / authorization exceptions.
	 *
	 * TODO possibly use more specific exception types and error messages
	 */
	@ExceptionHandler(ShiroException.class)
	public String shiroError(ShiroException ex) {

		this.log.error("Shiro error: {}", ex.getMessage());

		return this.serviceManager.getBean(UriService.class)
				.builder(Entities.USER, Actions.LOGIN)
				.addQueryParam("postLoginRedirect", true).buildRedirect();
	}

	/**
	 * Handles exceptions when user uploads exceed the maximum allowed upload size.
	 *
	 * The maximum allowed upload size can be configured in the properties
	 * {@code spring.http.multipart.max-file-size} and
	 * {@code spring.http.multipart.max-request-size}
	 */
	@ExceptionHandler(MultipartException.class)
	public String uploadError(MultipartException ex, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws URISyntaxException {

		this.log.info(ex.getMessage());

		// Since HistoryInterceptor#preHandle was not called, we need to retrieve the history like this
		// instead of from the request, this also makes sure the request is registered for proper
		// redirection
		History history = History.forRequest(request);

		List<String> errors = new ArrayList<>();
		errors.add(this.translationService.translate("user.error.upload_size",
				new Object[] { this.maxFileSize, this.maxRequestSize }));
		redirectAttributes.addFlashAttribute("errors", errors);

		return history.getRedirect();
	}
}
