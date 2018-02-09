/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UriUtils;

import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.controller.helper.history.HistoryEntry;

/**
 * Injects history information into the request/session.
 *
 * TODO split into multiple interceptors?
 */
public class HistoryInterceptor extends HandlerInterceptorAdapter {

	private Logger log = LoggerFactory.getLogger(HistoryInterceptor.class);

	/**
	 * Keeps track of and injects history information into request before the request is handled.
	 *
	 * TODO do not include pages that were redirected
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		History history = History.forRequest(request);

		// Decode redirection information
		String redirectParam = request.getParameter("redirect");
		String redirect = "/";
		HistoryEntry lastGetEntry =
				history.getLastByRequestMethod(RequestMethod.GET, false).orElse(null);

		// Use redirect URL parameter first, last history entry second and "/" as a last resort
		if (redirectParam != null) {
			redirect = UriUtils.decode(redirectParam, "UTF-8");
		} else if (lastGetEntry != null) {
			redirect = lastGetEntry.toString();
		}

		history.setRedirect("redirect:" + redirect);

		return super.preHandle(request, response, handler);
	}
}
