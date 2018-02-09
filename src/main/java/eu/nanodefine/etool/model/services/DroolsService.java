/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.drools.DroolsEventListener;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.ddos.DroolsDecisionObject;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDOMap;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.utilities.utils.ExplanationUtil;

/**
 * Service for drools-related processing.
 */
@Service
public class DroolsService implements IService {

	@Autowired
	private KieContainer kieContainer;

	private Logger log = LoggerFactory.getLogger(DroolsService.class);

	@Autowired
	private PerformanceConfiguration performanceConfiguration;

	@Autowired
	private PerformanceDictionary performanceDictionary;

	@Autowired
	private ServiceManager serviceManager;

	/**
	 * Creates a technique warning map for the given material.
	 *
	 * <p>Technique signifiers are mapped to a list of attributes for which warnings exist.
	 * Warnings exist for attributes with unknown values which, if the value was know, would change
	 * the recommendation of the technique.</p>
	 *
	 * TODO move to other service?
	 */
	private Map<String, List<Attribute>> createTechniqueMaterialWarningsMap(
			List<Technique> techniques, Material material) {
		Map<String, List<Attribute>> techniqueWarningsMap = new HashMap<>();
		List<Attribute> attributes;
		Attribute attribute;
		DroolsDecisionObject mcDdo;

		Map<String, PerformanceCriterionDDOMap> ddoMaps =
				Maps.uniqueIndex(this.performMatching(material.getMaterialCriterions()),
						PerformanceCriterionDDOMap::getTechniqueSignifier);

		PerformanceCriterionDDOMap ddoMap;
		PerformanceCriterionDDO pddo;
		for (Technique t : techniques) {
			ddoMap = ddoMaps.get(t.getSignifier());
			attributes = new ArrayList<>();
			for (MaterialCriterion mc : material.getMaterialCriterions()) {
				attribute = this.performanceDictionary.getAttribute(mc.getName());
				mcDdo = new MaterialCriterionDDO(mc);
				pddo = ddoMap.get(mc.getName());

				if (!mcDdo.isUnknown()
						|| !ExplanationUtil.hasIncompletenessDissuasion(pddo)) {
					continue;
				}

				attributes.add(attribute);
			}

			techniqueWarningsMap.put(ddoMap.getTechniqueSignifier(), attributes);
		}

		return techniqueWarningsMap;
	}

	/**
	 * Creates a map of technique warning maps for the given materials.
	 *
	 * @see #createTechniqueMaterialWarningsMap(List, Material)
	 *
	 * TODO move to other service?
	 */
	public Map<Material, Map<String, List<Attribute>>> createTechniqueMaterialWarningsMaps(
			List<Technique> techniques, Iterable<Material> materials) {
		Map<Material, Map<String, List<Attribute>>> warningsMaps = new HashMap<>();

		for (Material material : materials) {

			warningsMaps.put(material, this.serviceManager.getBean(DroolsService.class)
					.createTechniqueMaterialWarningsMap(techniques, material));
		}

		return warningsMaps;
	}

	/**
	 * Performs drools matching for the given material criteria.
	 */
	public List<PerformanceCriterionDDOMap> performMatching(
			Set<MaterialCriterion> materialCriteria) {

		// Get KIE session
		KieSession kieSession = this.kieContainer.newKieSession();
		this.log.info("new KIE session created");

		// Event listener
		kieSession.addEventListener(new DroolsEventListener());
		this.log.info("event listener added to KIE session");

		// Convert material criteria into drools decision
		MaterialCriterionDDOMap mMap =
				new MaterialCriterionDDOMap(materialCriteria);
		kieSession.insert(mMap);

		PerformanceCriterionDDOMap performance;
		// For backward-compatibility
		String materialSignifier = mMap.get(PerformanceAttributes.MATERIAL_SIGNIFIER) != null ?
				mMap.get(PerformanceAttributes.MATERIAL_SIGNIFIER).getValue() :
				PerformanceDictionary.DEFAULT_MATERIAL_SIGNIFIER;
		List<PerformanceCriterionDDOMap> performanceMaps = new Vector<>();
		for (ConfiguredPerformance cp : this.performanceConfiguration
				.getEntriesForMaterialSignifier(materialSignifier)) {
			performance = new PerformanceCriterionDDOMap(cp.getEntries());
			performanceMaps.add(performance);
			kieSession.insert(performance); // Insert as fact
		}

		// Insert ConfPerformanceDDO match container into session
		List<PerformanceCriterionDDOMap> matches = new Vector<>();
		kieSession.setGlobal("$matches", matches);

		// and fire the rules
		kieSession.fireAllRules();
		this.log.info("rules fired");

		// Mark matching maps
		matches.forEach(m -> m.setMatch(true));

		// and then dispose of the session
		kieSession.dispose();
		this.log.info("KIE session disposed");

		return performanceMaps;
	}
}
