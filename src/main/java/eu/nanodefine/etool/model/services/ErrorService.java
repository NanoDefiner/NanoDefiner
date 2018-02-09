/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for error-related processing.
 */
@Service
public class ErrorService implements IService {

	@Value("${build.date}")
	private String buildDate;

	@Value("${build.version}")
	private String version;

	/**
	 * Prepare a generic error page for the given request and response.
	 *
	 * TODO too much controller logic?
	 *
	 * TODO split into errorAttributeMap creation and the rest
	 */
	public Map<String, Object> prepareGenericErrorPage(HttpServletRequest request,
			HttpServletResponse response,
			ErrorAttributes errorAttributes, Optional<Session> session, Model model) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request, response);

		Map<String, Object> errorAttributesMap = errorAttributes
				.getErrorAttributes(requestAttributes, true);

		errorAttributesMap.put("status", response.getStatus());
		errorAttributesMap.put("error", HttpStatus.valueOf(response.getStatus()).getReasonPhrase());

		errorAttributesMap.put("Application version", this.version + " ("
				+ this.buildDate + ")");
		errorAttributesMap.put("User history",
				request.getAttribute("history"));
		model.addAttribute("errorAttributesMap", errorAttributesMap)
				.addAttribute("version", this.version)
				.addAttribute("buildDate", this.buildDate);

		session.ifPresent(s -> s.setAttribute("errorAttributesMap", errorAttributesMap));

		return errorAttributesMap;
	}
}
