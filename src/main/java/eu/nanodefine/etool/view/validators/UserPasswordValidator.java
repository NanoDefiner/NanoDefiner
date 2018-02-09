/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.UserRepository;

/**
 * Validator for password reset requests.
 */
@Component
public class UserPasswordValidator implements Validator {

	public final static String ERROR_EMPTY = "user.password.error.empty";

	public final static String ERROR_NOT_FOUND = "user.password.error.not_found";

	private final UserRepository userRepository;

	@Autowired
	public UserPasswordValidator(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Supports {@link User} objects as form-backing beans.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	/**
	 * Checks whether a user account can be found for either the provided email address or username.
	 */
	@Override
	public void validate(Object target, Errors errors) {
		User userForm = (User) target;

		String email = userForm.getEmail().trim();
		String username = userForm.getUsername().trim();

		if (username.isEmpty() && email.isEmpty()) {
			errors.reject(ERROR_EMPTY);
		}

		User user = new User();

		if (!email.isEmpty()) {
			user = this.userRepository.findFirstByEmail(email).orElse(new User());
		}

		if (user.getId() == null && !username.isEmpty()) {
			user = this.userRepository.findFirstByUsername(username).orElse(new User());
		}

		if (user.getId() == null) {
			errors.reject(ERROR_NOT_FOUND);
		}

		userForm.setId(user.getId());
	}
}
