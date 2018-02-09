/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.custom_attribute;

import java.util.Optional;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.repositories.CustomAttributeRepository;

/**
 * Abstract controller for {@link CustomAttribute} controllers.
 */
public class AbstractCustomAttributeController extends AbstractController {

	/**
	 * Loads custom attribute referenced in the request from the database
	 *
	 * <p>Extracts the {@link CustomAttribute} ID from the request path and loads it from the
	 * database if present. Otherwise, a {@link CustomAttribute} with ID 0 is returned.</p>
	 */
	@ModelAttribute(name = "customAttributePersisted", binding = false)
	public CustomAttribute customAttribute(@PathVariable Optional<Integer> customAttributeId) {

		CustomAttribute customAttribute = new CustomAttribute();
		customAttribute.setId(0);

		if (customAttributeId.isPresent()) {
			customAttribute = this.repositoryManager.getBean(CustomAttributeRepository.class)
					.findOne(customAttributeId.get());
		}

		return customAttribute;
	}

	/**
	 * Automatically extracts a custom attribute entity from the request
	 *
	 * <p>Extracts a custom attribute entity ({@link Dossier}, {@link Material} or
	 * {@link Method}) from the request based on the entities provided by
	 * {@link eu.nanodefine.etool.controller.advice.RequestIdsAdvice}. If no custom entity was
	 * found, a dossier with ID 0 is returned.</p>
	 */
	@ModelAttribute(name = "customAttributeEntity", binding = false)
	public ICustomAttributeEntity determineCustomAttributeEntity(@ModelAttribute Dossier dossier,
			@ModelAttribute Material material, @ModelAttribute Method method) {

		if (material.getId() != 0) {
			return material;
		}

		if (method.getId() != 0) {
			return method;
		}

		return dossier;
	}
}
