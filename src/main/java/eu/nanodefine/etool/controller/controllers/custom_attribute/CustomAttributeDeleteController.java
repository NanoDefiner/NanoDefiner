/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.custom_attribute;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.exception.runtime.InvalidRequestParametersException;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.services.CustomAttributeService;

/**
 * Controller for custom attribute deletion
 */
@Controller
@RequestMapping(Entities.CUSTOM_ATTRIBUTE + "/" + Actions.DELETE)
public class CustomAttributeDeleteController extends AbstractCustomAttributeController {

	/**
	 * Delete several custom attributes
	 *
	 * <p>Works with an array of request parameters to determine the custom attribute IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String deleteMany(
			@RequestParam(Entities.CUSTOM_ATTRIBUTE + "Ids") Integer[] customAttributeIds,
			@ModelAttribute("customAttributeEntity") ICustomAttributeEntity parentEntity,
			@RequestAttribute History history) {

		if (parentEntity.getId() == 0) {
			throw new InvalidRequestParametersException("Invalid parent entity.");
		}

		this.validateUserAwareEntities(parentEntity);

		this.serviceManager.getBean(CustomAttributeService.class)
				.deleteCustomAttributesByIds(parentEntity, customAttributeIds);

		return history.getRedirect();
	}

	/**
	 * Delete one custom attribute
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #deleteMany(Integer[], ICustomAttributeEntity, History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.CUSTOM_ATTRIBUTE + ".id={customAttributeId}")
	@RequiresUser
	public String deleteOne(@PathVariable Integer customAttributeId,
			@ModelAttribute("customAttributeEntity") ICustomAttributeEntity parentEntity,
			@RequestAttribute History history) {

		return this.deleteMany(new Integer[] { customAttributeId }, parentEntity, history);
	}

}
