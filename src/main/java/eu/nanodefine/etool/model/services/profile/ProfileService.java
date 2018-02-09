/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.profile;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.ProfileRepository;
import eu.nanodefine.etool.model.services.view.NumberService;
import eu.nanodefine.etool.model.services.view.TranslationService;

/**
 * Service for profile-related processing.
 */
@Service
public class ProfileService implements IService {

	private final ProfileRepository profileRepository;

	private final ServiceManager serviceManager;

	private final TranslationService translationService;

	@Autowired
	public ProfileService(ProfileRepository profileRepository, ServiceManager serviceManager,
			TranslationService translationService) {
		this.profileRepository = profileRepository;
		this.serviceManager = serviceManager;
		this.translationService = translationService;
	}

	/**
	 * Builds a profile map for the given user.
	 */
	@Transactional(readOnly = true)
	public Map<Technique, Profile> buildProfileMap(User user) {

		Map<Technique, Profile> profileMap = new HashMap<>();

		for (Profile p : this.profileRepository.findByUser(user)) {
			profileMap.put(p.getTechnique(), p);
		}

		return profileMap;
	}

	/**
	 * Formats the cost of the given profile.
	 */
	public String formatCost(Profile profile) {
		return profile != null ? this.formatNumericValue(profile.getCost())
				: this.translationService.translate("profile.table.value.not_available");
	}

	/**
	 * Formats the duration of the given profile.
	 */
	public String formatDuration(Profile profile) {
		return profile != null ? this.formatNumericValue(profile.getDuration())
				: this.translationService.translate("profile.table.value.not_available");
	}

	/**
	 * Formats the given numeric value (cost/duration).
	 */
	public String formatNumericValue(Double value) {
		return this.serviceManager.getBean(NumberService.class)
				.formatNumber(value, 0, ImmutableMap.of("0",
						this.translationService.translate("profile.table.value.not_available")));
	}

}
