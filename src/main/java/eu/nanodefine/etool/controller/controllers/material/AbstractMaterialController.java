/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.knowledge.configurations.ReferenceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredReference;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.model.services.material.MaterialService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.technique.TechniqueService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;

/**
 * Abstract material controller.
 *
 * <p>Provides access to common beans and model attributes.</p>
 */
public class AbstractMaterialController extends AbstractController {

	@Autowired
	protected MaterialService materialService;

	@Autowired
	protected MaterialTransactionalService materialTransactionalService;

	@Autowired
	private ReferenceConfiguration referenceConfiguration;

	/**
	 * Adds form data for material creation and update.
	 *
	 * <p>This is mainly for multiconstituent samples, where information about other materials are
	 * added to the material creation / update form technique modals.</p>
	 */
	protected void addFormData(List<Material> materials, Map<String, String> materialCriteriaMap,
			Model model) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		// Collect material compatibility information for each technique
		Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>
				compatibilityTableMap =
				mcs.createMaterialCriteriaTechniqueCompatibilityTableMap(materials);

		// Collect material description incompleteness information for each technique,
		Table<Material, String, Double> materialIncompletenessTable =
				this.materialService.createMaterialIncompletenessTable(materials);

		model.addAttribute("materialCriteria", materialCriteriaMap)
				.addAttribute("compatibilityTableMap", compatibilityTableMap)
				.addAttribute("materialIncompletenessTable", materialIncompletenessTable);
	}

	/**
	 * Determine default material name.
	 *
	 * TODO only used for create/update, move into sub-controller?
	 */
	@ModelAttribute("defaultMaterialName")
	public String defaultMaterialName(@ModelAttribute Dossier dossier,
			@ModelAttribute("templates") List<Material> templates) {

		String defaultMaterialName;

		if (dossier.getId() != 0) {
			// If the material is associated with a dossier, simply use "Particule component #X", where
			// X is the number of existing materials for the dossier + 1
			defaultMaterialName = String.format("%s #%d",
					this.translationService.translate(Entities.MATERIAL),
					// TODO extract into count query
					dossier.getMaterials().size() + 1);

		} else {

			// Otherwise use a global counter if it's a template material
			// TODO extract into proper locale string with arguments?
			defaultMaterialName = String.format("%s template #%d",
					this.translationService.translate(Entities.MATERIAL),
					templates.size() + 1);
		}

		return defaultMaterialName;
	}

	/**
	 * Add reference materials to model which are available as templates to choose from.
	 */
	@ModelAttribute("references")
	public List<ConfiguredReference> references() {
		return this.referenceConfiguration.getEntries();
	}

	/**
	 * Add techniques to model.
	 *
	 * <p>Filters and sorts techniques, and prepares the live feedback technique labels.</p>
	 */
	@ModelAttribute("techniques")
	@RequiresUser
	public List<Technique> techniques(@ModelAttribute Dossier dossier) {

		List<Technique> techniques = new ArrayList<>(this.techniques.size());

		// TODO solve cleaner and simpler

		// Tier names and their labels
		List<String> tierNames = // TODO load these from the sheet?
				Arrays.asList("tier1", "tier1_na", "tier2", "tier2_na");
		List<String> tiersLabels = Arrays.asList("T1", "T1*", "T2", "T2*");
		Map<String, Integer> techniqueRanking = new HashMap<>(techniques.size());

		for (Technique t : this.techniques) {

			Set<String> tiers = new HashSet<>();

			// Load technique tiers from performance configuration
			for (ConfiguredPerformance cp : this.performanceConfiguration.getEntries()) {
				if (cp.getEntry(PerformanceAttributes.TECHNIQUE_SIGNIFIER)
						.getValue().equals(t.getSignifier())) {
					tiers = ConfigurationUtil.toSet(cp.getEntry(PerformanceAttributes.TIER).getValue());
				}
			}

			String name = t.getName() + " ";

			// Ranking is based on the position in the tierNames and will be used to sort techniques
			int ranking = 0;
			int index;
			for (String tier : tiers) {
				if (tierNames.contains(tier)) {
					index = tierNames.indexOf(tier);
					name += "[" + tiersLabels.get(index) + "]";
					ranking += 1 + index;
				}
			}

			techniqueRanking.put(t.getSignifier(), ranking);

			Technique tNew = new Technique(t.getSignifier(), name,
					t.getComment(), null, null);
			tNew.setId(t.getId());

			techniques.add(tNew);
		}

		// Filter techniques
		techniques = this.serviceManager.getBean(TechniqueService.class)
				.filterTechniquesForDossier(techniques, dossier);

		// Sort techniques
		techniques.sort(Comparator.comparing(o -> techniqueRanking.get(o.getSignifier())));

		return techniques;
	}

	/**
	 * Adds user-defined material templates to the model.
	 */
	@ModelAttribute("templates")
	@RequiresUser
	public List<Material> templates() {
		return this.materialTransactionalService.loadUserTemplateMaterials(this.getCurrentUser());
	}

	/**
	 * Adds techniques to the model which the user has flagged as not available (lab settings).
	 */
	@ModelAttribute("unavailableTechniques")
	@RequiresUser
	public List<Technique> unavailableTechniques() {
		return this.serviceManager.getBean(TechniqueService.class)
				.loadUnavailableTechniquesForUser(this.getCurrentUser());
	}
}
