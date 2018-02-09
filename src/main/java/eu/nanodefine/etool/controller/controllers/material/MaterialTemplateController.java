/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.knowledge.configurations.ReferenceConfiguration;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;

/**
 * Controller for material templates.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/template/" + Entities.MATERIAL
		+ ".id={materialId}")
public class MaterialTemplateController extends AbstractMaterialController {

	@Autowired
	private ReferenceConfiguration referenceConfiguration;

	/**
	 * Loads material templates into the MCS
	 *
	 * <p>Uses {@link RequestMethod#POST} to avoid eligibility for redirects.</p>
	 */
	@PostMapping
	@RequiresUser
	@ResponseBody
	public Map<String, Object[]> get(@ModelAttribute Material material, @PathVariable String materialId) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		if (material.getId() != 0) {
			this.validateUserAwareEntities(material);
			// If the material is user-defined, load it from the database
			return mcs.createMaterialCriteriaFormMap(material);
		} else {
			// Otherwise load it from the configuration
			return mcs.createMaterialCriteriaFormMap(this.referenceConfiguration.getEntry(materialId));
		}
	}
}
