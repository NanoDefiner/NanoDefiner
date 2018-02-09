/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.user;

import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.view.validators.UserLoginValidator;

/**
 * Controller for user login / logout.
 */
@Controller
@RequestMapping("/" + Entities.USER + "/")
public class UserLogInOutController extends AbstractUserController {

	@Autowired
	private UserLoginValidator userValidator;

	/**
	 * Set up form validation.
	 */
	@InitBinder("userLogin")
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("username", "passwordHash", "remembered");
		binder.setValidator(this.userValidator);
	}

	/**
	 * Shows the login form.
	 */
	@GetMapping(value = Actions.LOGIN)
	public String login(@RequestParam(name = "postLoginRedirect",
			required = false, defaultValue = "0")
			boolean postLoginRedirect, Model model,
			@RequestAttribute History history) {
		// TODO extract into method
		if (SecurityUtils.getSubject() != null &&
				SecurityUtils.getSubject().getPrincipal() != null) {
			return this.uriService.builder(Entities.DASHBOARD, Actions.READ)
					.buildRedirect();
		}

		model.addAttribute("postLoginRedirect", postLoginRedirect ?
				history.getPrevious().toString() : "/" + Entities.DASHBOARD);

		return Templates.USER_LOGIN;
	}

	/**
	 * Attempts to authenticate the user.
	 *
	 * <p>This is called after the user has submitted the login form and will
	 * attempt to authenticate the user and redirect them to the dashboard on
	 * success or to the login form otherwise.</p>
	 */
	@PostMapping(value = Actions.LOGIN)
	public String loginPost(@ModelAttribute("userLogin") @Valid User userLogin,
			BindingResult result, Model model,
			@RequestParam(name = "postLoginRedirect", required = false,
					defaultValue = "/" + Entities.DASHBOARD) String postLoginRedirect) {

		if (!result.hasErrors()) {
			User user = this.userService.updateUserAfterLogin(userLogin);

			// Store user object in the session for availability
			this.securityRealm.getShiroSubject().getSession().setAttribute(Entities.USER, user);
			this.securityRealm.getShiroSubject().getSession().setTimeout(-1);

			this.log.info("subject with principal '" + user.getUsername()
					+ "' has been authenticated and logged in");
		}

		if (this.securityRealm.getShiroSubject().isAuthenticated()
				|| this.securityRealm.getShiroSubject().isRemembered()) {
			return "redirect:" + postLoginRedirect;
		}

		model.addAttribute("username", userLogin.getUsername());

		return Templates.USER_LOGIN;
	}

	/**
	 * Performs a logout for the user.
	 *
	 * <p>After a successful logout, the user is redirected to the index page.</p>
	 */
	@RequiresUser
	@RequestMapping(Actions.LOGOUT)
	public String logout() {
		Subject subject = this.securityRealm.getShiroSubject();
		String principal = subject.getPrincipal().toString();
		subject.logout();
		this.log.info("subject '{}' logged out", principal);
		return "redirect:/";
	}

	/**
	 * Form-backing bean for user login.
	 */
	@ModelAttribute("userLogin")
	public User userLogin() {
		return new User();
	}
}
