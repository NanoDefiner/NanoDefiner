/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.user;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;
import eu.nanodefine.etool.model.services.profile.ProfileService;

/**
 * Controller for updating user profile information.
 */
@Controller
@RequestMapping("/" + Entities.USER + "/" + Actions.UPDATE)
public class UserUpdateController extends AbstractUserController {

	@Autowired
	private ProfileService profileService;

	@Autowired
	private TechniqueRepository techniqueRepository;

	@Resource(name = "techniques")
	private List<Technique> techniques;

	/**
	 * Shows the edit profile form.
	 */
	@GetMapping
	@RequiresAuthentication
	public String get(Model model) {

		User user = this.userRepository.findOne(this.getCurrentUser().getId());

		model.addAttribute(user).addAttribute("profileMap", this.profileService.buildProfileMap(user));

		return Templates.USER_UPDATE;
	}

	/**
	 * Persist profile changes.
	 */
	@PostMapping
	@RequiresAuthentication
	public String post(Model model, @Valid User user, BindingResult result,
			@RequestParam String password, @RequestParam String passwordRepeat,
			@RequestAttribute("errors") Collection<String> errors,
			@RequestAttribute("successes") Collection<String> successes,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchAlgorithmException {

		User userSession = this.getCurrentUser();

		user.setPasswordHash(userSession.getPasswordHash());
		user.setHashFunction(userSession.getHashFunction());
		if (!password.equals("")) {
			if (password.length() == 0) { // TODO can it have length 0 but != ""?
				errors.add("user.create.error.password.length");
			} else if (!passwordRepeat.equals(password)) {
				errors.add("user.create.error.password.mismatch");
			} else {

				this.userService.createPassword(user, password);
			}
		}

		// TODO validation of technique settings?
		User userPersisted;

		if (!result.hasErrors() && errors.isEmpty()) {
			userPersisted = this.userService.updateUser(user, userSession.getId());
			//this.profileService.updateProfiles(userPersisted, this.techniques,
			//request.getParameterMap());
			successes.add("user.update.success");
		} else {
			userPersisted = this.userRepository.findOne(userSession.getId());
		}

		this.securityRealm.getShiroSubject().getSession().setAttribute(Entities.USER, userPersisted);

		model.addAttribute(userPersisted);

		this.updateLocale(request, response);

		return this.uriService.builder(Entities.USER, Actions.UPDATE).buildRedirect();
	}

	/**
	 * Add techniques to model for lab settings.
	 */
	@ModelAttribute("techniques")
	public List<Technique> techniques() {
		return this.techniques;
	}

}
