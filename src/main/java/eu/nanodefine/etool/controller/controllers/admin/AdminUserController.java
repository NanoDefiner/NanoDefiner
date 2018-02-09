/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.admin;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.UserRepository;
import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for user management.
 *
 * <p>Requires admin role.</p>
 */
@Controller
@RequestMapping(Entities.ADMIN + "/" + Entities.USER)
public class AdminUserController extends AbstractAdminController {

	/**
	 * Add user management actions to the model.
	 *
	 * <p>Admins can create new users.</p>
	 */
	@ModelAttribute
	public void dossierActionList(
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {
		String path = this.uriService.builder(Entities.ADMIN, "user_create").build();

		actionList.add(new ActionListEntry(path, "admin.users.user_create"));
	}

	/**
	 * Display user management tables.
	 */
	@GetMapping
	@RequiresRoles({ "admin" })
	public String get(Model model) {
		model.addAttribute("usersNotActivated",
				this.serviceManager.getBean(UserService.class).loadNotActivatedUsers());
		model.addAttribute("users", this.repositoryManager.getBean(UserRepository.class).findAll());

		return Templates.ADMIN_USER;
	}

	/**
	 * Activate users.
	 *
	 * <p>Users are activated and activation confirmation mails are sent.</p>
	 *
	 * TODO move to other controller?
	 */
	@PostMapping
	@RequiresRoles({ "admin" })
	public String post(@RequestParam Integer[] userIds,
			@RequestAttribute History history) {

		List<User> users = this.userService.loadUsersByIds(userIds);

		this.userService.activateUsers(users);
		this.userService.sendActivationConfirmationMail(users);

		return this.uriService.builder(Entities.ADMIN, Entities.USER).setAnchor("activate")
				.buildRedirect();
	}
}
