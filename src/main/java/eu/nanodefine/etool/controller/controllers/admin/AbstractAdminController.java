/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.UserRepository;
import eu.nanodefine.etool.model.services.user.UserService;

/**
 * Abstract controller for administration activities.
 */
public class AbstractAdminController extends AbstractController {

	@Autowired
	UserService userService;

	/**
	 * Create redirect to the user management page.
	 */
	String createUserManagementRedirect() {
		return this.uriService.builder(Entities.ADMIN, Entities.USER).setAnchor("management")
				.buildRedirect();
	}

	/**
	 * Makes the user available in the model.
	 *
	 * @see eu.nanodefine.etool.controller.advice.RequestIdsAdvice
	 */
	@ModelAttribute(binding = false)
	public User user(@PathVariable("userId") Optional<Integer> userIdPath,
			@RequestParam(name = Entities.USER + ".id") Optional<Integer> userIdRequest) {

		User user = null;
		Integer userId = userIdPath.orElse(userIdRequest.orElse(0));

		if (userId != 0) {
			user = this.repositoryManager.getBean(UserRepository.class).findOne(userId);
		}

		if (user == null) {
			user = new User();
			user.setId(0);
		}

		return user;
	}
}
