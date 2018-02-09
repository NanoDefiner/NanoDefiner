/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.custom_attribute;

import java.util.List;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.services.CustomAttributeService;
import eu.nanodefine.etool.utilities.classes.UriBuilder;

/**
 * Controller for changing custom attributes.
 */
@Controller
@RequestMapping(Entities.CUSTOM_ATTRIBUTE + "/" + Actions.UPDATE + "/"
		+ Entities.CUSTOM_ATTRIBUTE + ".id={customAttributeId}")
public class CustomAttributeUpdateController extends AbstractCustomAttributeController {

	/**
	 * Displays the custom attribute creation form with values from the selected attribute filled in
	 */
	@GetMapping
	@RequiresUser
	public String get(
			@ModelAttribute(name = "customAttributeEntity") ICustomAttributeEntity entity,
			@ModelAttribute("customAttributePersisted") CustomAttribute customAttributePersisted,
			Model model) {

		this.validateUserAwareEntities(entity);

		// Add it as "customAttribute" for our form
		model.addAttribute(customAttributePersisted);

		return Templates.CUSTOM_ATTRIBUTE_CREATE;
	}

	/**
	 * Persists custom attribute changes
	 *
	 * <p>Behaves like
	 * {@link CustomAttributeCreateController#post(ICustomAttributeEntity, CustomAttribute, Model,
	 * String, MultipartFile, String, List)}, the only difference being that here, for unchanged
	 * file values, the old file is taken again instead of forcing the user to re-upload the file.</p>
	 */
	@PostMapping
	@RequiresUser
	public String post(
			@ModelAttribute(name = "customAttributeEntity") ICustomAttributeEntity entity,
			@Valid CustomAttribute customAttributeForm,
			@RequestParam(name = "value_text", required = false) String valueText,
			@RequestParam(name = "value_file", required = false) MultipartFile valueFile,
			@ModelAttribute("customAttributePersisted") CustomAttribute customAttributePersisted,
			@RequestParam String submit, Model model,
			@RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(entity);

		// Attempts to update the attribute
		this.serviceManager.getBean(CustomAttributeService.class)
				.updateCustomAttribute(entity, customAttributeForm, customAttributePersisted,
						this.getCurrentUser(), valueText, valueFile, errors);

		// Display form and errors
		if (!errors.isEmpty()) {
			model.addAttribute("customAttributeForm", customAttributePersisted);

			return Templates.CUSTOM_ATTRIBUTE_CREATE;
		}

		// Display form or entity page
		if (submit.equals("next")) {
			return UriBuilder.create(Entities.CUSTOM_ATTRIBUTE, Actions.CREATE)
					.addEntityIdQuery(entity).buildRedirect();
		} else {
			return UriBuilder.create(entity.getEntityType(), Actions.READ)
					.addEntityId(entity).setAnchor(Entities.CUSTOM_ATTRIBUTE + "s").buildRedirect();
		}

	}

}
