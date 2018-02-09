/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.interceptors;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Interceptor for messages displayed to the user.
 *
 * <p>To make handling of success/error messages easier, this interceptor automatically adds
 * certain request attributes which can be used during controller processing and will be appended
 * as flash parameters to the response.</p>
 *
 * <p>To use these messages in controllers, add a parameter like:
 *
 * {@code @RequestAttribute("errors") List<String> errors}
 *
 * This list can then be changed and will automatically be carried over to the next page on
 * redirect. If a messages does not contain spaces, it will automatically be treated as a locale
 * string and translated.</p>
 */
public class MessageInterceptor extends HandlerInterceptorAdapter {

	private final Logger log = LoggerFactory.getLogger(MessageInterceptor.class);

	/**
	 * Message types.
	 */
	private final String[] messageTypes = { "messages", "errors", "successes" };

	/**
	 * After request processing, add attributes to flash map if it's a redirect.
	 */
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {

		// In case of a redirect, add flash attributes for messages
		if (modelAndView != null &&
				modelAndView.getViewName().startsWith("redirect:")) {
			FlashMap flashMap = new FlashMap();

			for (String messageType : this.messageTypes) {
				flashMap.put(messageType, request.getAttribute(messageType));
			}

			FlashMapManager flashMapManager = RequestContextUtils
					.getFlashMapManager(request);
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
		}
	}

	/**
	 * Before the request is processed, inject lists for each message type into the request.
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		for (String messageType : this.messageTypes) {
			if (request.getAttribute(messageType) == null) {
				request.setAttribute(messageType, new ArrayList<String>());
			}
		}

		return true;
	}

}
