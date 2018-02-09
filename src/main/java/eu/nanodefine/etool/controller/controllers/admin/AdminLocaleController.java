/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.admin;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.utilities.locale.CoverageTrackingMessageSource;

/**
 * Controller for locale coverage.
 */
@Controller
@RequestMapping(Entities.ADMIN + "/locale")
public class AdminLocaleController extends AbstractController {

	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;

	/**
	 * Shows the locale coverage page.
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String localeTest(Model model) {
		model.addAttribute("localeCoverageEnabled",
				this.messageSource instanceof CoverageTrackingMessageSource);
		model.addAttribute("messageSource", this.messageSource);

		return Templates.ADMIN_LOCALE;
	}

}
