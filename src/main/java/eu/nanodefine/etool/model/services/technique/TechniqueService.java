/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.technique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.helpers.predicates.dossier.IDossierAwarePredicate;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;
import eu.nanodefine.etool.model.services.DroolsService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;
import eu.nanodefine.etool.utilities.utils.ExplanationUtil;

/**
 * Service for technique-related processing.
 */
@Service
public class TechniqueService implements IService {

	private final PerformanceConfiguration performanceConfiguration;

	private final ServiceManager serviceManager;

	private final List<IDossierAwarePredicate<Technique>> techniquePredicates;

	private final TechniqueRepository techniqueRepository;

	private Logger log = LoggerFactory.getLogger(TechniqueService.class);

	@Autowired
	public TechniqueService(PerformanceConfiguration performanceConfiguration,
			ServiceManager serviceManager, List<IDossierAwarePredicate<Technique>> techniquePredicates,
			TechniqueRepository techniqueRepository) {
		this.performanceConfiguration = performanceConfiguration;
		this.serviceManager = serviceManager;
		this.techniquePredicates = techniquePredicates;
		this.techniqueRepository = techniqueRepository;
	}

	/**
	 * Converts tiers from Strings into sortable numeric representations.
	 *
	 * <p>The numeric representations are as follows:</p>
	 *
	 * <ul>
	 *   <li>tier 1: 10</li>
	 *   <li>tier 1, not assessed: 11</li>
	 *   <li>tier 2: 20</li>
	 *   <li>tier 2, not assessed: 21</li>
	 *   <li>non-tiered: 99</li>
	 * </ul>
	 */
	public Set<Integer> convertTiersToInt(Set<String> tiers) {
		Set<Integer> tiersInt = new HashSet<>(tiers.size() + 1);

		Integer tierInt;
		for (String tier : tiers) {
			// TODO extract into constant mapping
			if (tier.contains("tier1")) {
				tierInt = 10;
			} else if (tier.contains("tier2")) {
				tierInt = 20;
			} else {
				this.log.warn("Invalid tier: {}", tier);
				continue;
			}

			if (tier.contains("_na")) {
				tierInt += 1;
			}

			tiersInt.add(tierInt);
		}

		// Non-tiered MTs
		if (tiersInt.isEmpty()) {
			tiersInt.add(99);
		}

		return tiersInt;
	}

	/**
	 * Aggregates a map of material warning maps into a table.
	 *
	 * <p>The original mapping is map(material -> map(technique signifier -> attributes), and the
	 * returned mapping is table(technique signifier, attribute -> materials).</p>
	 */
	public Table<String, Attribute, List<Material>> createAggregatedTechniqueMaterialWarnings(
			Map<Material, Map<String, List<Attribute>>> warningsMaps) {

		Table<String, Attribute, List<Material>> aggregatedWarnings =
				HashBasedTable.create();

		Material material;
		String techniqueSignifier;

		// Iterate over attribute-specific technique warnings map for each material
		for (Map.Entry<Material, Map<String, List<Attribute>>> warningsMap
				: warningsMaps.entrySet()) {

			material = warningsMap.getKey();

			// Iterate over warnings map for one technique
			for (Map.Entry<String, List<Attribute>> techniqueWarnings
					: warningsMap.getValue().entrySet()) {

				techniqueSignifier = techniqueWarnings.getKey();

				// Iterate over attributes in the technique warnings map
				for (Attribute attribute : techniqueWarnings.getValue()) {

					// Create empty list of materials for which a warning exists for the
					// given attribute and technique
					if (!aggregatedWarnings.contains(techniqueSignifier, attribute)) {
						aggregatedWarnings.put(techniqueSignifier, attribute,
								new ArrayList<>());
					}

					// Add current material to the list
					aggregatedWarnings.get(techniqueSignifier, attribute)
							.add(material);
				}
			}
		}

		return aggregatedWarnings;
	}

	/**
	 * Creates a technique material warnings table for the given techniques and materials.
	 *
	 * @see DroolsService#createTechniqueMaterialWarningsMap(List, Material)
	 */
	public Table<String, Attribute, List<Material>> createAggregatedTechniqueMaterialWarnings(
			List<Technique> techniques, Iterable<Material> materials) {
		return this.createAggregatedTechniqueMaterialWarnings(
				this.serviceManager.getBean(DroolsService.class)
						.createTechniqueMaterialWarningsMaps(techniques, materials));
	}

	/**
	 * Creates a listing of techniques filtered for the given multiconstituent state and purpose.
	 *
	 * @see #createTechniqueNameListing(List)
	 */
	public String createFilteredTechniqueNameListing(List<Technique> techniques,
			boolean multiconstituent, String purpose) {

		return this.createTechniqueNameListing(this.filterTechniques(techniques, multiconstituent,
				purpose));
	}

	/**
	 * Creates a map of disclaimers for the given techniques.
	 *
	 * The mapping is map(technique signifier -> disclaimer text)
	 */
	public Map<String, String> createTechniqueGeneralWarningsMap(
			List<Technique> techniques) {

		Map<String, String> warningsMap = new HashMap<>();

		for (Technique technique : techniques) {
			String techniqueDisclaimer = this.performanceConfiguration
					.getEntry(technique.getSignifier())
					.getEntry(PerformanceAttributes.DISCLAIMER)
					.getValue();

			// TODO solve cleaner
			if (!techniqueDisclaimer.equals("")) {
				warningsMap.put(technique.getSignifier(),
						techniqueDisclaimer);
			}
		}

		return warningsMap;
	}

	/**
	 * Creates the incompleteness map for the techniques.
	 *
	 * <p>The result maps technique signifiers to a list of incompletenesses:</p>
	 *
	 * <ul>
	 * <li>Technique incompleteness</li>
	 * <li>Weighted technique incompleteness</li>
	 * </ul>
	 */
	public Map<String, List<Double>> createTechniqueIncompletenessMap() {

		Map<String, List<Double>> incompletenessMap = new HashMap<>();

		List<Double> incompletenesses;
		PerformanceCriterionDDOMap performance;
		for (ConfiguredPerformance cp : this.performanceConfiguration.getEntries()) {
			incompletenesses = new ArrayList<>(2);
			performance = new PerformanceCriterionDDOMap(cp.getEntries());
			incompletenesses.add(ExplanationUtil.techniqueIncompletenessPotential(performance));
			incompletenesses.add(ExplanationUtil.weightedTechniqueIncomplenessPotential(performance));

			incompletenessMap.put(performance.getTechniqueSignifier(), incompletenesses);
		}

		return incompletenessMap;
	}

	/**
	 * Creates a listing of technique names separates by commas from the given techniques.
	 *
	 * TODO move to template service
	 */
	public String createTechniqueNameListing(List<Technique> techniques) {
		return techniques.stream().map(Technique::getName).collect(Collectors.joining(", "));
	}

	/**
	 * Filters the given techniques for a dossier with given multiconstituent state and purpose.
	 *
	 * @see #filterTechniquesForDossier(List, Dossier)
	 */
	public List<Technique> filterTechniques(List<Technique> techniques, boolean multiconstituent,
			String purpose) {
		return this.filterTechniquesForDossier(techniques,
				new Dossier(null, null, multiconstituent, false, purpose));
	}

	/**
	 * Filter the given techniques for the given dossier.
	 *
	 * <p>Some techniques are only available for monoconstituent samples, others are only available
	 * for specific purposes.</p>
	 *
	 * @see IDossierAwarePredicate
	 */
	public List<Technique> filterTechniquesForDossier(List<Technique> techniques,
			Dossier dossier) {

		if (dossier.getId() != null && dossier.getId() == 0) {
			return techniques;
		}

		List<Technique> techniquesFiltered = new ArrayList<>(techniques);

		// Filter techniques
		for (IDossierAwarePredicate<Technique> predicate : this.techniquePredicates) {
			predicate.setDossier(dossier);
			techniquesFiltered = Lists.newArrayList(
					Collections2.filter(techniquesFiltered, predicate));
		}

		return techniquesFiltered;
	}

	/**
	 * Returns the tiers of the given technique.
	 */
	public Set<String> getTiers(Technique technique) {
		return ConfigurationUtil.toSet(this.performanceConfiguration
				.getEntry(technique.getSignifier()).getEntry(PerformanceAttributes.TIER).getValue());
	}

	/**
	 * Returns whether the given technique has warnings based on the map of technique disclaimers and
	 * the table of technique material warnings.
	 */
	public boolean hasTechniqueWarnings(Technique t,
			Map<String, String> techniqueGeneralWarnings,
			Table<String, Attribute, List<Material>> techniqueMaterialWarnings) {

		return techniqueGeneralWarnings.containsKey(t.getSignifier())
				|| !techniqueMaterialWarnings.row(t.getSignifier()).isEmpty();
	}

	/**
	 * Returns whether the given technique is tier 1.
	 */
	public boolean isTier1(Technique technique) {
		Set<String> tiers = this.getTiers(technique);

		for (Integer tier : this.convertTiersToInt(tiers)) {
			if (tier < 20) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the given technique is unassessed.
	 */
	public boolean isUnassessed(Technique technique) {
		return this.performanceConfiguration
				.getEntry(technique.getSignifier()).getEntry(PerformanceAttributes.TIER)
				.getValue().contains("_na");
	}

	/**
	 * Loads unavailable technique for the given user based on their profile settings.
	 */
	@Transactional(readOnly = true)
	public List<Technique> loadUnavailableTechniquesForUser(User user) {
		return this.techniqueRepository.findByProfilesUserAndProfilesEnabledFalse(user);
	}

	/**
	 * Returns whether the given technique requires a plausibility check.
	 */
	public boolean needsPlausibilityCheck(Technique technique) {
		// TODO extract into constant
		return !technique.getSignifier().equals("BET") && this.isTier1(technique);
	}
}
