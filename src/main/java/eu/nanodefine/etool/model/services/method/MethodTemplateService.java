/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.services.profile.ProfileTemplateService;
import eu.nanodefine.etool.model.services.view.NumberService;
import eu.nanodefine.etool.model.services.view.TemplateService;
import eu.nanodefine.etool.model.services.view.UriService;

/**
 * Template service for method-related processing.
 */
@Service("methodTS")
public class MethodTemplateService extends TemplateService {

	@Autowired
	public MethodTemplateService(ServiceManager serviceManager) {
		super(serviceManager);
	}

	/**
	 * Builds a URI for method creation.
	 */
	public String buildMethodCreateURI(Dossier dossier, String tier) {
		return this.serviceManager.getBean(UriService.class).builder(Entities.METHOD, Actions.CREATE)
				.addEntityId(dossier)
				.addPathParam(Entities.METHOD + '.' + PerformanceAttributes.TIER, tier)
				.build();
	}

	/**
	 * Determines how the method uncertainty will be displayed.
	 *
	 * @see ProfileTemplateService#determineUncertainty(Double, boolean)
	 */
	public String determineUncertainty(Method method, boolean percentSign) {
		return this.serviceManager.getBean(ProfileTemplateService.class)
				.determineUncertainty(method.getTechniqueUncertainty(), percentSign);
	}

	/**
	 * Formats the result of a method.
	 *
	 * <p>Returns "–" if the method has no result.</p>
	 */
	public String formatResult(Method method) {
		return method.hasResult() ? this.serviceManager.getBean(NumberService.class)
				.formatNumber(method.getNumericResult(), 0) + (method.getDataFormat().equals("VSSA") ? "m²/cm³" : "nm") : "–";
	}

}
