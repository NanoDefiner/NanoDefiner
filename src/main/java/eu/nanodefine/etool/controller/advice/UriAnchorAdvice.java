/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.advice;

import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Extracts anchor information from the request and passes it into the model
 */
@ControllerAdvice
public class UriAnchorAdvice {

	/**
	 * Looks for anchor information in the request
	 *
	 * <p>To remember where exactly a user was on any given page, the URL anchor is automatically
	 * appended as a request parameter.</p>
	 */
	@ModelAttribute("anchor")
	public String anchor(@RequestParam Optional<String> anchor) {
		// TODO is this necessary and/or can it cause problems?
		return StringEscapeUtils.escapeHtml4(anchor.orElse(""));
	}
}
