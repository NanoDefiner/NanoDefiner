/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.admin;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.exception.runtime.InvalidRequestParametersException;
import eu.nanodefine.etool.model.dto.User;

/**
 * User management controller.
 */
@Controller
@RequestMapping(Entities.ADMIN + "/" + Entities.USER + "/" + Entities.USER + ".id={userId}")
public class AdminUserManagementController extends AbstractAdminController {

	/**
	 * Activates a user.
	 */
	@RequestMapping("/activate")
	@RequiresRoles({ "admin" })
	public String activateUser(@ModelAttribute User user) {
		this.checkUser(user);

		this.userService.activateUser(user);
		this.userService.sendActivationConfirmationMail(ImmutableList.of(user));

		return this.createUserManagementRedirect();
	}

	/**
	 * Give a user admin permissions.
	 */
	@RequestMapping("/add_admin")
	@RequiresRoles({ "admin" })
	public String addAdmin(@ModelAttribute User user) {
		this.checkUser(user);

		this.userService.addRole(user, "admin");

		return this.createUserManagementRedirect();
	}

	private void checkUser(User user) {
		if (user.getId() == 0) {
			throw new InvalidRequestParametersException("Invalid user ID");
		}
	}

	/**
	 * Deactivates a user.
	 */
	@RequestMapping("/deactivate")
	@RequiresRoles({ "admin" })
	public String deactivateUser(@ModelAttribute User user) {
		this.checkUser(user);

		this.userService.deactivateUser(user);

		return this.createUserManagementRedirect();
	}

	/**
	 * Revokes admin permissions for a user.
	 */
	@RequestMapping("/remove_admin")
	@RequiresRoles({ "admin" })
	public String removeAdmin(@ModelAttribute User user) {
		this.checkUser(user);

		this.userService.removeRole(user, "admin");

		return this.createUserManagementRedirect();
	}

}
