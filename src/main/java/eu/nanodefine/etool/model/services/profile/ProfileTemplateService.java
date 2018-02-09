/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.profile;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.view.TranslationService;

/**
 * Service for for technique-related processing within templates.
 */
@Service("PTS")
public class ProfileTemplateService implements IService {

	private final ServiceManager serviceManager;

	@Autowired
	public ProfileTemplateService(
			ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	/**
	 * Determines the displayed uncertainty of the given profile.
	 *
	 * @see #determineUncertainty(Double, boolean)
	 */
	public String determineUncertainty(@Nullable Profile profile, boolean percentSign) {
		Double uncertainty = profile != null ? profile.getUncertainty() : 0;

		return this.determineUncertainty(uncertainty, percentSign);
	}

	/**
	 * Determines the displayed uncertainty for the given numeric uncertainty.
	 *
	 * <p>An uncertainty of 0 is not possible and represents the "unknown" state.</p>
	 */
	public String determineUncertainty(Double uncertainty, boolean percentSign) {
		return uncertainty > 0 ?
				uncertainty.toString() + (percentSign ? "%" : "") :
				this.serviceManager.getBean(TranslationService.class).translate("global.value.unknown");
	}
}
