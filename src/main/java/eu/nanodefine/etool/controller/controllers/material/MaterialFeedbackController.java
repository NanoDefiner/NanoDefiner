/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDOMap;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.services.DroolsService;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;

/**
 * Controller for live feedback during material creation and update.
 */
@Controller
@RequestMapping("/" + Entities.MATERIAL + "/feedback/" + Entities.DOSSIER
		+ ".id={dossierId}")
public class MaterialFeedbackController extends AbstractMaterialController {

	/**
	 * Suitability constants.
	 *
	 * TODO move somewhere else
	 */
	public final static String SUITABILITY_NO = "danger";

	public final static String SUITABILITY_UNCERTAIN = "warning";

	public final static String SUITABILITY_YES = "success";

	/**
	 * Matches the given material criterions against the performance criterions for each technique.
	 *
	 * <p>This method will be called via Ajax each time material creation form values change, and
	 * returns an updated compatibility table.</p>
	 */
	@PostMapping
	@ResponseBody
	@RequiresUser
	public Map<String, Object> post(HttpServletRequest request, @ModelAttribute Dossier dossier,
			@ModelAttribute Material material) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		// First extract material criteria from request
		Set<MaterialCriterion> materialCriteria = mcs
				.createMaterialCriteriaFromRequestParameters(request.getParameterMap());

		List<Map<String, PerformanceCriterionDDOMap>> matches = new ArrayList<>();

		// Match material criteria against performance criteria
		matches.add(mcs.convertPerformanceCriterionDDOMapListToMap(
				this.serviceManager.getBean(DroolsService.class)
						.performMatching(materialCriteria)));

		// Create compatibility table based on the matches (only for active material)
		Table<String, String, MaterialCriterionTechniqueCompatibility> compatibilityTable =
				mcs.createMaterialCriteriaTechniqueCompatibilityTable(materialCriteria, matches.get(0));

		// Consider other dossier materials if necessary
		if (dossier.getId() != 0) {
			this.validateUserAwareEntities(dossier);
			List<Material> materials = material.getId() != 0 ?
					this.materialTransactionalService.loadOtherNotArchivedDossierMaterials(material) :
					this.materialTransactionalService.loadNotArchivedDossierMaterials(dossier);

			matches.addAll(this.materialService.collectMatches(materials));
		}

		// Create incompleteness map
		Map<String, Double> incompletenessMap = this.materialService.createMaterialIncompletenessMap(
				new MaterialCriterionDDOMap(materialCriteria));

		Map<String, Object> responseBody = new HashMap<>();
		// Create suitability map (for all assoicated materials)
		responseBody.put("suitability", this.materialService
				.createTechniqueSuitabilityMap(matches, incompletenessMap));
		// Convert compatibility table to map for JSON response
		responseBody.put("compatibilityMap", this.materialService
				.convertCompatibilityTableToJsonMap(compatibilityTable));
		// Add incompleteness map
		responseBody.put("incompleteness", incompletenessMap);

		// Add request counter so that the client can keep track of request/response order
		responseBody.put("meta", ImmutableMap.of("i", request.getParameter("_i")));

		return responseBody;
	}

}
