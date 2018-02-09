/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.security.NanoDefinerRealm;
import eu.nanodefine.etool.view.form.UserForm;

/**
 * Abstract super class for user creation / update form validation.
 */
abstract public class UserFormValidator implements Validator {

	@Autowired
	protected NanoDefinerRealm securityRealm;

	@Autowired
	protected UserService userService;

	/**
	 * The username must not be empty during both creation and updating of user accounts.
	 */
	void performEmptyChecks(Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username",
				"user.create.error.username.length");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email",
				"user.create.error.email");
	}

	/**
	 * During user creation, and when a new password is created during user updating, the password
	 * must be repeated correctly.
	 */
	void performPasswordRepeatCheck(UserForm userForm, Errors errors) {
		if (!userForm.getPasswordRepeat().equals(userForm.getPassword())) {
			errors.rejectValue("passwordRepeat", "user.create.error.password.mismatch");
		}
	}

	/**
	 * During user creation, username uniqueness must be checked.
	 */
	void performUniqueUsernameCheck(UserForm userForm, Errors errors) {
		if (!this.userService.isNewUserUnique(userForm)) {
			errors.rejectValue("username", "user.create.error.username.in_use");
		}
	}

	/**
	 * Validators are compatible with the {@link UserForm} form-backing bean.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return UserForm.class.isAssignableFrom(clazz);
	}

}
