/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;

import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.repositories.UserRepository;
import eu.nanodefine.etool.model.services.user.UserService;

/**
 * Abstract user controller.
 *
 * <p>Grants access to user service and repository beans.</p>
 */
public class AbstractUserController extends AbstractController {

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected UserService userService;
}
