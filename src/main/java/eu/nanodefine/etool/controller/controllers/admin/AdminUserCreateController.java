/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.admin;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.view.form.UserForm;
import eu.nanodefine.etool.view.validators.UserCreateFormValidator;

/**
 * Controller for user creation by admins.
 */
@Controller
@RequestMapping(Entities.ADMIN + "/" + Entities.USER + "_" + Actions.CREATE)
public class AdminUserCreateController extends AbstractAdminController {

	@Autowired
	private UserCreateFormValidator userCreateFormValidator;

	/**
	 * Shows user creation form.
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String get(Model model) {

		model.addAttribute(new UserForm());

		return Templates.USER_CREATE;
	}

	/**
	 * Set up form validation.
	 */
	@InitBinder("userForm")
	public void initUserCreateFormBinder(WebDataBinder binder) {
		binder.setValidator(this.userCreateFormValidator);
	}

	/**
	 * Processed user creation form.
	 */
	@PostMapping
	public String post(@ModelAttribute @Valid UserForm userForm, BindingResult result,
			@RequestAttribute("successes") List<String> successes) throws NoSuchAlgorithmException {

		if (result.hasErrors()) {
			return Templates.USER_CREATE;
		}

		UserService userService = this.serviceManager.getBean(UserService.class);

		User userPersisted = userService.createUser(userForm);

		userService.activateUser(userPersisted);

		successes.add("admin.user_update.success.created");

		return this.createUserManagementRedirect();
	}

}
