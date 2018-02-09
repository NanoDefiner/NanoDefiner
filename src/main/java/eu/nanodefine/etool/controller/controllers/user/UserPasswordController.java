/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.user;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresGuest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.view.validators.UserPasswordValidator;

/**
 * Controller for password reset / recovery.
 */
@Controller
@RequestMapping("/" + Entities.USER + "/password/")
public class UserPasswordController extends AbstractUserController {

	@Autowired
	private UserPasswordValidator userValidator;

	/**
	 * Check whether the reset request is valid.
	 *
	 * <p>The reset request is valid if the user has requested a password reset and has not logged
	 * in since, and if the given code equals the activation code in the database.</p>
	 */
	private void checkResetRequest(User user, String code, List<String> errors) {

		if (user.getActivationCode() == null || !code.equals(user.getActivationCode())) {
			errors.add("user.password.error.reset");
		}
	}

	/**
	 * Set up form validation and binding.
	 */
	@InitBinder("userForm")
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("username", "email");
		binder.setValidator(this.userValidator);
	}

	/**
	 * Show password reset form.
	 */
	@GetMapping(value = "request")
	@RequiresGuest
	public String request() {
		return Templates.USER_PASSWORD;
	}

	/**
	 * Request password reset link.
	 *
	 * <p>If the request is valid, i.e. a user could be found for the given username or e-mail
	 * address, a mail will be sent with a reset link.</p>
	 */
	@PostMapping(value = "request")
	@RequiresGuest
	public String requestPost(@ModelAttribute("userForm") @Valid User userForm,
			BindingResult result, @RequestAttribute("successes") List<String> successes)
			throws NoSuchAlgorithmException {
		if (result.hasErrors()) {
			return Templates.USER_PASSWORD;
		}

		User userPersisted = this.userRepository.findOne(userForm.getId());

		// Validator should have made sure that this is not null
		assert userPersisted != null;

		// Generate code, will be deleted on login
		userPersisted = this.userService.createPasswordResetToken(userPersisted);

		// Send mail
		this.userService.sendPasswordResetMail(userPersisted);

		// Display success
		successes.add("user.password.success.mail.sent");
		// Redirect to front page
		return "redirect:/";
	}

	/**
	 * Show password reset form.
	 */
	@GetMapping(value = Entities.USER + ".id={userId}/code={code}")
	@RequiresGuest
	public String reset(@ModelAttribute User user, @PathVariable String code,
			@RequestAttribute("errors") List<String> errors) {
		// TODO avoid code repetition
		this.checkResetRequest(user, code, errors);

		if (!errors.isEmpty()) {
			return "redirect:/";
		}

		return Templates.USER_PASSWORD_RESET;
	}

	/**
	 * Set new password.
	 */
	@PostMapping(value = Entities.USER + ".id={userId}/code={code}")
	@RequiresGuest
	public String resetPost(@ModelAttribute User user, @PathVariable String code,
			@RequestParam String passwordNew,
			@RequestParam String passwordNewRepeated,
			@RequestAttribute("errors") List<String> errors,
			@RequestAttribute("successes") List<String> successes) {
		// TODO avoid code repetition
		this.checkResetRequest(user, code, errors);

		// TODO uniform password validation, see UserCreateFormValidator
		if (passwordNew.length() < 1) {
			errors.add("user.create.error.password.length");
		} else if (!passwordNew.equals(passwordNewRepeated)) {
			errors.add("user.create.error.password.mismatch");
		}

		if (!errors.isEmpty()) {
			return Templates.USER_PASSWORD_RESET;
		}

		this.userService.createPassword(user, passwordNew);
		this.userService.updateUser(user, user.getId());

		successes.add("user.password.success.reset");

		return this.uriService.builder(Entities.USER, Actions.LOGIN).buildRedirect();
	}

	/**
	 * Add user to model from the path variable.
	 */
	@ModelAttribute
	public User user(@PathVariable("userId") Optional<Integer> userId) {

		User user = Optional.ofNullable(this.userRepository.findOne(userId.orElse(0)))
				.orElse(new User());

		if (user.getId() == null) {
			user.setId(0);
		}

		return user;
	}

	/**
	 * Form-backing bean.
	 */
	@ModelAttribute("userForm")
	public User userForm() {
		return new User();
	}

}
