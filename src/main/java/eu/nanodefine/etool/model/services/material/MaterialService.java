/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.controllers.material.MaterialFeedbackController;
import eu.nanodefine.etool.knowledge.configurations.ExplanationConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredExplanation;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDOMap;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.DroolsService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;
import eu.nanodefine.etool.utilities.utils.ExplanationUtil;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;

/**
 * Service for material-related processing.
 *
 * TODO split off methods that need transactional into separate service
 */
@Service
public class MaterialService implements IService {

	private final ExplanationConfiguration explanationConfiguration;

	private final PerformanceConfiguration performanceConfiguration;

	private final PerformanceDictionary performanceDictionary;

	private final ServiceManager serviceManager;

	private Logger log = LoggerFactory.getLogger(MaterialService.class);

	@Resource(name = "techniques")
	private List<Technique> techniques;

	@Autowired
	public MaterialService(ExplanationConfiguration explanationConfiguration,
			PerformanceConfiguration performanceConfiguration,
			PerformanceDictionary performanceDictionary,
			ServiceManager serviceManager) {
		this.explanationConfiguration = explanationConfiguration;
		this.performanceConfiguration = performanceConfiguration;
		this.performanceDictionary = performanceDictionary;
		this.serviceManager = serviceManager;
	}

	/**
	 * Performs drools matching for a collection of materials and collects the result in a map.
	 *
	 * TODO move to different service?
	 */
	public Collection<Map<String, PerformanceCriterionDDOMap>> collectMatches(
			Iterable<Material> materials) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);
		Collection<Map<String, PerformanceCriterionDDOMap>> ddoMapCollection = new ArrayList<>();

		for (Material m : materials) {
			ddoMapCollection.add(mcs.convertPerformanceCriterionDDOMapListToMap(this.serviceManager
					.getBean(DroolsService.class).performMatching(m.getMaterialCriterions())));
		}

		return ddoMapCollection;
	}

	/**
	 * Converts a material/technique compatibility table to a map suitable for JSON output.
	 *
	 * <p>Structure of the resulting map:</p>
	 *
	 * <ul>
	 *   <li>material signifier -> (technique signifier -> (match reason, explanation))</li>
	 *   <li>material signifier -> ("value" -> translated material criterion value)</li>
	 * </ul>
	 */
	public Map<String, Map<String, List<String>>> convertCompatibilityTableToJsonMap(
			Table<String, String, MaterialCriterionTechniqueCompatibility> table) {

		Map<String, Map<String, List<String>>> map = new HashMap<>();
		Map<String, List<String>> entry;

		PerformanceCriterionDDO pddo;
		MaterialCriterionDDO mddo;
		for (MaterialCriterionTechniqueCompatibility compatibility : table.values()) {

			mddo = compatibility.getMaterialCriterion();
			pddo = compatibility.getPerformanceCriterion();

			if (!map.containsKey(mddo.getName())) {
				entry = new HashMap<>();
				// Translated material criterion value
				entry.put("value", ImmutableList.of(this.performanceDictionary
						.translateValue(mddo.getName(), mddo.getValue())));

				map.put(mddo.getName(), entry);
			}

			// Add compatibility for technique
			map.get(mddo.getName()).put(pddo.getTechniqueSignifier(),
					ImmutableList.of(compatibility.getMatchReason(),
							compatibility.getExplanation()));
		}

		return map;
	}

	/**
	 * Creates a map of material/technique incompleteness measures for the given material criteria.
	 *
	 * <p>The material incompleteness is the percentage of unknown material properties.</p>
	 *
	 * <p>The weighted material incompleteness incorporates a technique-specific weight for each
	 * material property into the calculation.</p>
	 *
	 * @see ExplanationUtil#materialIncompletenessPotential(MaterialCriterionDDOMap)
	 * @see ExplanationUtil#weightedMaterialIncompletenessPotential(PerformanceCriterionDDOMap, MaterialCriterionDDOMap)
	 */
	public Map<String, Double> createMaterialIncompletenessMap(
			MaterialCriterionDDOMap materialCriterionDDOMap) {

		Map<String, Double> incompletenessMap = new HashMap<>();

		Double materialIncompleteness = ExplanationUtil
				.materialIncompletenessPotential(materialCriterionDDOMap);
		incompletenessMap.put("material", materialIncompleteness);

		PerformanceCriterionDDOMap performance;
		Double materialTechniqueIncompleteness;
		for (ConfiguredPerformance cp : this.performanceConfiguration.getEntries()) {
			performance = new PerformanceCriterionDDOMap(cp.getEntries());

			materialTechniqueIncompleteness = ExplanationUtil
					.weightedMaterialIncompletenessPotential(performance, materialCriterionDDOMap);

			incompletenessMap.put(performance.getTechniqueSignifier(), materialTechniqueIncompleteness);

		}

		return incompletenessMap;
	}

	/**
	 * Creates a table of material/technique incompleteness measures for the given materials.
	 *
	 * @see #createMaterialIncompletenessMap(MaterialCriterionDDOMap)
	 */
	public Table<Material, String, Double> createMaterialIncompletenessTable(
			List<Material> materials) {

		Table<Material, String, Double> incompletenessTable =
				HashBasedTable.create(materials.size(), this.techniques.size());

		MaterialCriterionDDOMap materialCriterionDDOMap;
		Double materialIncompleteness, materialTechniqueIncompleteness;

		for (Material material : materials) {

			materialCriterionDDOMap = new MaterialCriterionDDOMap(material.getMaterialCriterions());
			materialIncompleteness = ExplanationUtil
					.materialIncompletenessPotential(materialCriterionDDOMap);
			incompletenessTable.put(material, "material", materialIncompleteness);

			PerformanceCriterionDDOMap performance;
			for (ConfiguredPerformance cp : this.performanceConfiguration.getEntries()) {
				performance = new PerformanceCriterionDDOMap(cp.getEntries());

				materialTechniqueIncompleteness =
						ExplanationUtil.weightedMaterialIncompletenessPotential(performance,
								materialCriterionDDOMap);

				incompletenessTable.put(material, performance.getTechniqueSignifier(),
						materialTechniqueIncompleteness);
			}
		}

		return incompletenessTable;
	}

	/**
	 * Create technique suitability map for the given materials and incompleteness map.
	 */
	public Map<String, String> createTechniqueSuitabilityMap(
			Iterable<Material> materials) {
		return this.createTechniqueSuitabilityMap(this.collectMatches(materials), null);
	}

	/**
	 * Creates a {@link Map} of techniques and their suitability in regards to
	 * the given set of material criteria.
	 *
	 * TODO move to different service?
	 */
	public Map<String, String> createTechniqueSuitabilityMap(
			Map<String, PerformanceCriterionDDOMap> ddoMaps) {

		// Check signifiers against configured techniques
		Map<String, String> suitableTechniques = new HashMap<>();
		for (Technique technique : this.techniques) {
			// TODO is it possible that a signifier is not in our ddoMaps?
			suitableTechniques.put(technique.getSignifier(),
					(ddoMaps.get(technique.getSignifier()).getMatch())
							? MaterialFeedbackController.SUITABILITY_YES
							: MaterialFeedbackController.SUITABILITY_NO);
		}

		if (ddoMaps.size() > suitableTechniques.size()) {
			this.log.warn("INCONSISTENCY DETECTED - match for unknown technique(s)");
			Set<String> matchSignifiers = new HashSet<>(ddoMaps.keySet());
			matchSignifiers.removeAll(suitableTechniques.keySet());
			for (String signifier : matchSignifiers) {
				this.log.warn("{} - unknown technique signifier", signifier);
			}
		}

		return suitableTechniques;
	}

	/**
	 * Aggregates technique suitability information for several materials, taking into account
	 * incompleteness information.
	 *
	 * <p>For each material, a suitability map is created using
	 * {@link #createTechniqueSuitabilityMap(Map)}. The information from these maps is aggregated
	 * into a single map in which a technique is marked as suitable only if it is suitable to all
	 * materials.</p>
	 *
	 * <p>Suitable techniques are only recommended if the weighted material incompleteness does
	 * not exceed a certain threshold (0.5).</p>
	 *
	 * @see #createTechniqueSuitabilityMap(Map)
	 */
	public Map<String, String> createTechniqueSuitabilityMap(
			Collection<Map<String, PerformanceCriterionDDOMap>> ddoMapCollection,
			Map<String, Double> incompletenessMap) {

		Map<String, String> techniqueSuitability = new HashMap<>();

		// Aggregate suitability for all materials
		for (Map<String, PerformanceCriterionDDOMap> ddoMap : ddoMapCollection) {

			Map<String, String> suitabilityMap = this.createTechniqueSuitabilityMap(ddoMap);

			for (Entry<String, String> entry : suitabilityMap.entrySet()) {
				// If the technique does not yet exists in our map, add it
				// If the technique was marked as suitable, add it again in case it is
				// not suitable for the given material
				if (!techniqueSuitability.containsKey(entry.getKey())
						|| techniqueSuitability.get(entry.getKey())
						.equals(MaterialFeedbackController.SUITABILITY_YES)) {
					techniqueSuitability.put(entry.getKey(),
							entry.getValue());
				}
			}
		}

		// Update suitability for techniques with too high weighted incompleteness
		// TODO solve cleaner for cases with several PCs
		if (incompletenessMap != null) {
			for (Entry<String, Double> entry : incompletenessMap.entrySet()) {
				// TODO extract constant
				if (entry.getValue() > .5 && techniqueSuitability.containsKey(entry.getKey())
						&& techniqueSuitability.get(entry.getKey())
						.equals(MaterialFeedbackController.SUITABILITY_YES)) {
					techniqueSuitability.put(entry.getKey(),
							MaterialFeedbackController.SUITABILITY_UNCERTAIN);
				}

			}
		}

		return techniqueSuitability;
	}

	/**
	 * Determines the collective expected size range for a collection of materials.
	 *
	 * <p>Returns the lowest lower size range and the highest upper size range of the materials.</p>
	 */
	public Double[] determineExpectedSizeRange(Iterable<Material> materials) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);
		Double[] sizeRangeAll = new Double[] { null, null };
		Double[] sizeRangeCurrent = null;

		for (Material m : materials) {
			sizeRangeCurrent = ConfigurationUtil.toInterval(
					mcs.createMaterialCriteriaMap(m, false).get(PerformanceAttributes.WR_SIZE_RANGE));

			if (sizeRangeCurrent != null) {
				sizeRangeAll[0] = (sizeRangeAll[0] == null || sizeRangeAll[0] > sizeRangeCurrent[0]) ?
						sizeRangeCurrent[0] :
						sizeRangeAll[0];

				sizeRangeAll[1] = (sizeRangeAll[1] == null || sizeRangeAll[1] < sizeRangeCurrent[1]) ?
						sizeRangeCurrent[1] :
						sizeRangeAll[1];
			}
		}

		return sizeRangeAll;
	}

	/**
	 * Determines the explanation for the given performance criterion.
	 *
	 * @see #determineExplanation(String, ConfiguredExplanation)
	 *
	 * TODO move to other service
	 */
	public String determineExplanation(PerformanceCriterionDDO ddo) {
		return this.determineExplanation(ddo.getMatchReason(),
				this.explanationConfiguration.getEntry(ddo.getName()));
	}

	/**
	 * Determines the explanation for the given explanation status and configured explanation.
	 *
	 * <p>This simply maps the constants from {@link ExplanationDictionary} to the methods of
	 * {@link ConfiguredExplanation}.</p>
	 *
	 * TODO move to other service
	 */
	public String determineExplanation(String explanationStatus,
			ConfiguredExplanation configuredExplanation) {
		if (configuredExplanation == null) {
			return "";
		}

		String explanation;
		// TODO refactor

		switch (explanationStatus) {
			case ExplanationDictionary.MATCH:
				explanation = configuredExplanation.getMatch();
				break;
			case ExplanationDictionary.MISMATCH:
				explanation = configuredExplanation.getMismatch();
				break;
			case ExplanationDictionary.IRRELEVANCE:
				explanation = configuredExplanation.getIrrelevance();
				break;
			case ExplanationDictionary.INCOMPLETENESS_MATERIAL:
				explanation = configuredExplanation.getIncompletenessMaterial();
				break;
			case ExplanationDictionary.INCOMPLETENESS_TECHNIQUE:
				explanation = configuredExplanation.getIncompletenessTechnique();
				break;
			default:
				explanation = "";
				this.log.warn("Invalid explanation status: \"{}\"",
						explanationStatus);
		}

		return explanation;
	}
}
