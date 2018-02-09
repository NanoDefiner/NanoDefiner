/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import eu.nanodefine.etool.view.form.UserForm;

/**
 * Validator for {@link UserForm} instances during user update processes.
 */
@Component
public class UserUpdateFormValidator extends UserFormValidator {

	@Override
	public void validate(Object target, Errors errors) {
		this.performEmptyChecks(errors);

		if (errors.hasErrors()) {
			return;
		}

		UserForm userForm = (UserForm) target;

		this.performPasswordRepeatCheck(userForm, errors);
	}
}
