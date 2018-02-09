/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.validators;

import java.util.Optional;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.UserRepository;
import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.security.NanoDefinerRealm;

/**
 * Validator for the user login form.
 */
@Component
public class UserLoginValidator implements Validator {

	private Logger log = LoggerFactory.getLogger(UserLoginValidator.class);

	@Autowired
	private NanoDefinerRealm securityRealm;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	/**
	 * Supports {@link User} objects as form-backing beans.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	/**
	 * Checks the form input validity and tries to perform a login for the user.
	 *
	 * TODO make sure the password can not be accessed later on
	 */
	@Override
	public void validate(Object target, Errors errors) {

		// Check for empty username / password
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username",
				"user.login.error.generic");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordHash",
				"user.login.error.generic");

		if (errors.hasErrors()) {
			return;
		}

		User userLogin = (User) target;

		try {

			Optional<User> userOptional = this.userRepository
					.findFirstByUsername(userLogin.getUsername());

			User user = userOptional.orElseThrow(UnknownAccountException::new);

			// TODO solve cleaner, error should be added in catch clause, needs
			// custom exception
			if (!user.isActivated()) {
				//errors.add("user.login.error.deactivated");

				throw new AuthenticationException("Account deactivated");
			}

			UsernamePasswordToken token = this.userService
					.buildAuthenticationToken(user, userLogin.getPasswordHash());
			token.setRememberMe(userLogin.isRemembered());

			// Authenticate currently executing user
			this.securityRealm.getShiroSubject().login(token);

			token.clear();

		} catch (UnknownAccountException uae) {
			// TODO Prepare adequate error page message
			this.log.info(uae.getMessage());
			errors.rejectValue("username", "user.login.error.generic");
		} catch (IncorrectCredentialsException ice) {
			// TODO Prepare adequate error page message
			this.log.info("password for user '{}' is incorrect",
					userLogin.getUsername());
			errors.rejectValue("passwordHash", "user.login.error.generic");
		} catch (LockedAccountException lae) {
			// TODO Implement locked account feature in realm
			// TODO Prepare adequate error page message
			this.log.info("account for user '{}' is locked", userLogin.getUsername());
			errors.rejectValue("username", "user.login.error.generic");
		} catch (ExcessiveAttemptsException eae) {
			// TODO Implement lock on massive login attempts
			// TODO Prepare adequate error page message
			this.log.info("excessive login attempts for user '{}' detected",
					userLogin.getUsername());
			errors.rejectValue("username", "user.login.error.generic");
		} catch (AuthenticationException ae) {
			// TODO Prepare adequate error page message
			this.log.info("authentication for user '{}' failed: {}",
					userLogin.getUsername(), ae.getMessage());
			errors.reject("user.login.error.generic");
		}
	}

}
