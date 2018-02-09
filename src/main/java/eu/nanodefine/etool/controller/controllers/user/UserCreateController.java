/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.user;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresGuest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.services.MailService;
import eu.nanodefine.etool.view.form.UserForm;
import eu.nanodefine.etool.view.validators.UserCreateFormValidator;

/**
 * Controller which handles creation user accounts.
 *
 * <p>It handles creation and activation of user accounts.</p>
 */
@Controller
@RequestMapping(Entities.USER + "/" + Actions.CREATE)
public class
UserCreateController extends AbstractUserController {

	@Autowired
	private UserCreateFormValidator userCreateFormValidator;

	/**
	 * Activate user account.
	 *
	 * <p>Only available to guests.</p>
	 */
	@RequiresGuest
	@GetMapping("/" + Actions.ACTIVATE + "/" + Entities.USER + ".id" + "={id}" + "/code={code}")
	public String activate(@PathVariable("id") Integer id,
			@PathVariable("code") String code,
			@RequestAttribute("errors") Collection<String> errors,
			@RequestAttribute("successes") Collection<String> successes) {

		User user = this.userRepository.findOne(id);

		// TODO move to service
		if (user == null) {
			this.log.error("User not found");
			errors.add("user.activate.error");
		} else if (user.getActivationDate() != null) {
			this.log.error("Account is already activated");
			errors.add("user.activate.error");
		} else if (!code.equals(user.getActivationCode())) {
			this.log.error("Invalid activation code");
			errors.add("user.activate.error");
		} else {
			this.userService.activateUser(user);

			successes.add("user.activate.success");
		}

		return this.uriService.builder(Entities.USER, Actions.LOGIN).buildRedirect();
	}

	/**
	 * Set up form validation.
	 */
	@InitBinder("userForm")
	public void initUserCreateFormBinder(WebDataBinder binder) {
		binder.setValidator(this.userCreateFormValidator);
	}

	/**
	 * Shows the registration form.
	 */
	@RequiresGuest
	@GetMapping
	public String register() {
		return Templates.USER_CREATE;
	}

	/**
	 * Handles registration form submission.
	 */
	@RequiresGuest
	@PostMapping
	public String registerPost(Model model,
			@ModelAttribute @Valid UserForm userForm, BindingResult result,
			@ModelAttribute("sendActivationMail") boolean sendActivationMail,
			@ModelAttribute("sendAdminMailUserCreated") boolean sendAdminMailUserCreated,
			@RequestAttribute("successes") Collection<String> successes)
			throws NoSuchAlgorithmException {

		// If errors occured, show form again
		if (result.hasErrors()) {
			return Templates.USER_CREATE;
		}

		User user = this.userService.createUser(userForm);

		this.log.info("User {} successfully created", user.getUsername());

		model.addAttribute(user);

		if (sendAdminMailUserCreated) {
			this.serviceManager.getBean(MailService.class).sendAdminMail("mail/user_created",
					this.translationService.translate("mail.user_create.subject"), model);
		}

		if (sendActivationMail) {
			this.serviceManager.getBean(MailService.class).sendAdminMail("mail/user_activate",
					this.translationService.translate("mail.user_activate.subject"), model);
		}

		successes.add("user.create.success.activation." + (sendActivationMail ? "mail" : "manual"));

		return "redirect:/";
	}

	/**
	 * Form-backing bean for user registration
	 */
	@ModelAttribute
	public UserForm userForm() {
		return new UserForm();
	}
}
