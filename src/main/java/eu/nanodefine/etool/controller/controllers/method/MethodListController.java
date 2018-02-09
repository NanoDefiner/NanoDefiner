/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Method;

/**
 * Controller for displaying the method list.
 */
@Controller
@RequestMapping("/" + Entities.METHOD + "/" + Actions.LIST)
public class MethodListController extends AbstractMethodController {

	/**
	 * Show list of user methods.
	 *
	 * <p>Methods are separated into active, archived, and all methods.</p>
	 */
	@GetMapping
	@RequiresUser
	public String get(Model model) {
		// TODO pagination?
		List<Method> methodsAll = this.methodService
				.loadUserMethods(this.getCurrentUser()),
				methodsActive = new ArrayList<>(),
				methodsArchived = new ArrayList<>();

		// Separate methods
		for (Method m : methodsAll) {
			if (m.isArchived() || m.getDossier().isArchived()) {
				methodsArchived.add(m);
			} else {
				methodsActive.add(m);
			}
		}

		// Add data to model
		model.addAttribute("methodsAll", methodsAll)
				.addAttribute("methodsActive", methodsActive)
				.addAttribute("methodsArchived", methodsArchived);

		return Templates.METHOD_LIST;
	}
}
