/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.profile;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.repositories.ProfileRepository;

/**
 * Controller for updating user lab settings (profiles).
 *
 * TODO move?
 */
@Controller
@RequestMapping(Entities.PROFILE + "/" + Actions.UPDATE + "/"
		+ Entities.TECHNIQUE + ".id={techniqueId}")
public class ProfileController extends AbstractController {

	/**
	 * Show profile update form.
	 */
	@GetMapping
	public String get(@ModelAttribute Profile profile) {
		return Templates.PROFILE_UPDATE;
	}

	/**
	 * Set up form binding.
	 */
	@InitBinder("profile")
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields("enabled", "cost", "duration", "uncertainty");
	}

	/**
	 * Persist profile changes.
	 */
	@PostMapping
	public String post(@ModelAttribute @Valid Profile profile, BindingResult result, Model model) {

		if (!result.hasErrors()) {
			this.repositoryManager.getBean(ProfileRepository.class).save(profile);

			return this.uriService.builder(Entities.USER, Actions.UPDATE).setAnchor("lab")
					.buildRedirect();
		}

		return this.get(profile);
	}

	/**
	 * Add technique profile to model.
	 */
	@ModelAttribute
	public Profile profile(@ModelAttribute Technique technique) {
		Optional<Profile> profileOptional =
				this.repositoryManager.getBean(ProfileRepository.class)
						.findByUserAndTechnique(this.getCurrentUser(), technique);

		return profileOptional
				.orElseGet(() -> new Profile(technique, this.getCurrentUser(), true, 0., 0., 0.));
	}
}
