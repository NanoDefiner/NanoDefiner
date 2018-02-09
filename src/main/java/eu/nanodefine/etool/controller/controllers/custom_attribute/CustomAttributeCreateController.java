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
 * Controller for custom attribute creation.
 */
@Controller
@RequestMapping(Entities.CUSTOM_ATTRIBUTE + "/" + Actions.CREATE)
public class CustomAttributeCreateController extends AbstractCustomAttributeController {

	/**
	 * Displays the custom attribute creation form
	 *
	 * Requires a {@link ICustomAttributeEntity} to be present in the request for which this
	 * custom attribute will be created.
	 */
	@RequiresUser
	@GetMapping
	public String get(@ModelAttribute(name = "customAttributeEntity") ICustomAttributeEntity entity,
			Model model) {

		this.validateUserAwareEntities(entity);

		model.addAttribute(new CustomAttribute());

		return Templates.CUSTOM_ATTRIBUTE_CREATE;
	}

	/**
	 * Persists a custom attribute.
	 *
	 * <p>Further steps:</p>
	 *
	 * <ul>
	 * <li>If errors were encountered or the user wants to create another custom attribute,
	 * display the form again</li>
	 * <li>Otherwise return to the custom attribute entity overview</li>
	 * </ul>
	 */
	@RequiresUser
	@PostMapping
	public String post(
			@ModelAttribute(name = "customAttributeEntity") ICustomAttributeEntity entity,
			@Valid CustomAttribute customAttribute, Model model,
			@RequestParam(name = "value_text", required = false) String valueText,
			@RequestParam(name = "value_file", required = false) MultipartFile valueFile,
			@RequestParam(name = "submit") String submit,
			@RequestAttribute("errors") List<String> errors) {

		assert (valueText != null || valueFile != null);

		this.validateUserAwareEntities(entity);

		// Attempt to persist attribute
		CustomAttribute customAttributePersisted =
				this.serviceManager.getBean(CustomAttributeService.class).persistCustomAttribute(entity,
						customAttribute, this.getCurrentUser(), valueText, valueFile, errors);

		// Display form and error
		if (!errors.isEmpty()) {
			model.addAttribute(customAttributePersisted);

			return Templates.CUSTOM_ATTRIBUTE_CREATE;
		}

		// Redirect to new attribute creation form
		if (submit.equals("next")) {
			return UriBuilder.create(Entities.CUSTOM_ATTRIBUTE, Actions.CREATE)
					.addEntityIdQuery(entity).buildRedirect();
		} else {
			// Go back to custom attribute entity page
			return UriBuilder.create(entity.getEntityType(), Actions.READ)
					.addEntityId(entity).setAnchor(Entities.CUSTOM_ATTRIBUTE + "s").buildRedirect();
		}
	}

}
