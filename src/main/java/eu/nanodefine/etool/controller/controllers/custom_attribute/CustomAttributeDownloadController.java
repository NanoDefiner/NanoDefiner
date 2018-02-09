/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.custom_attribute;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.services.FileService;

/**
 * Controller for custom attribute file download
 */
@Controller
@RequestMapping(Entities.CUSTOM_ATTRIBUTE + "/download/"
		+ Entities.CUSTOM_ATTRIBUTE + ".id={customAttributeId}")
public class CustomAttributeDownloadController extends AbstractCustomAttributeController {

	/**
	 * Serves the file associated to a custom attribute
	 *
	 * <p>Will display an error if the custom attribute has no file.</p>
	 *
	 * TOOD validation
	 */
	@GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@RequiresUser
	@ResponseBody
	public FileSystemResource download(
			@ModelAttribute("customAttributeEntity") ICustomAttributeEntity parentEntity,
			@ModelAttribute("customAttributePersisted") CustomAttribute customAttribute,
			HttpServletResponse response) {
		FileService fs = this.serviceManager.getBean(FileService.class);

		this.validateUserAwareEntities(parentEntity);

		// TODO proper error page
		if (!customAttribute.isFile()) {
			throw new RuntimeException("Chosen custom attribute has no file.");
		}

		String fileName = this.serviceManager.getBean(FileService.class)
				.extractOriginalFileName(customAttribute.getValue());

		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		return new FileSystemResource(
				fs.getFullPathInAnalysisDirectory(customAttribute.getValue()).toFile());
	}

}
