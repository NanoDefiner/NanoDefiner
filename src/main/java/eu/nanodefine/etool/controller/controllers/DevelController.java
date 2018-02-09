/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;

/**
 * Controller for the front page.
 */
@Controller
@RequestMapping
public class DevelController extends AbstractController {

	@Value("${build.date}")
	private String buildDate;

	@Value("${build.version}")
	private String version;

	/**
	 * Add the build date to the model for display on the front page.
	 */
	@ModelAttribute("buildDate")
	private String buildDate() {
		return this.buildDate;
	}

	/**
	 * Display the index page.
	 */
	@RequestMapping({ "/", "/" + Entities.INDEX + "/" + Actions.READ })
	public String get() {
		return Templates.INDEX_DEVEL;
	}

	/**
	 * Add version information to the model for display on the front page.
	 */
	@ModelAttribute("version")
	private String version() {
		return this.version;
	}
}
