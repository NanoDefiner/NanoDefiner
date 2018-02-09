/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import eu.nanodefine.etool.view.form.UserForm;

/**
 * Validator for {@link UserForm} instances during user creation processes.
 */
@Component
public class UserCreateFormValidator extends UserFormValidator {

	@Override
	public void validate(Object target, Errors errors) {
		this.performEmptyChecks(errors);
		// Password must not be empty during user creation
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password",
				"user.create.error.password.length");

		if (errors.hasErrors()) {
			return;
		}

		UserForm userForm = (UserForm) target;

		this.performUniqueUsernameCheck(userForm, errors);

		this.performPasswordRepeatCheck(userForm, errors);
	}
}
