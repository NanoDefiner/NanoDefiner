/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.knowledge.configurations.TechniqueConfiguration;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Option;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.MethodAnalysisServiceManager;
import eu.nanodefine.etool.model.helpers.method.Tier;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.services.material.MaterialService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.analysis.IAnalysisDataFormatService;
import eu.nanodefine.etool.model.services.profile.ProfileService;
import eu.nanodefine.etool.model.services.technique.TechniqueService;

/**
 * Abstract method controller.
 */
public abstract class AbstractMethodController extends AbstractController {

	@Autowired
	protected MethodAnalysisServiceManager analysisServiceManager;

	@Autowired
	protected MethodRepository methodRepository;

	@Autowired
	protected MethodService methodService;

	@Autowired
	protected TechniqueConfiguration techniqueConfiguration;

	/**
	 * Creates the redirect to the analysis tab of the method overview.
	 */
	public String buildRedirect(Method method) {
		return this.uriService.builder(Entities.METHOD, Actions.READ)
				.addEntityId(method).setAnchor("analysis").buildRedirect();
	}

	@ModelAttribute("methodAnalysisService")
	public IAnalysisDataFormatService methodAnalysisService(@ModelAttribute Method method) {
		return this.analysisServiceManager.getBean(method.getDataFormat());
	}

	/**
	 * Exposes the method for binding.
	 *
	 * Exposes {@link eu.nanodefine.etool.controller.advice.RequestIdsAdvice#method(Optional,
	 * Optional)} to overwrite {@code binding = false}.
	 */
	@ModelAttribute("methodForm")
	public Method methodForm(@ModelAttribute Method method) {
		return method;
	}

	@ModelAttribute("methodState")
	public Integer methodState(@ModelAttribute Method method,
			@ModelAttribute("methodAnalysisService") IAnalysisDataFormatService analysisService) {
		return analysisService.determineState(method);
	}

	/**
	 * Add pre-processing options to the model.
	 *
	 * TODO replace with more dynamic version and no longer automatically add
	 * to model then
	 */
	@ModelAttribute("preprocessingProtocols")
	public List<Option> preprocessingProtocols() {

		List<Option> options = new ArrayList<>();

		options.add(new Option("", this.translationService
				.translate("method.create.form.preprocessing.nothing")));

		options.addAll(this.performanceDictionary
				.getAttribute(PerformanceAttributes.PREPARATION).getOptions());

		return options;
	}

	/**
	 * Adds the user profile map to the model.
	 *
	 * @see Profile
	 */
	@ModelAttribute("profileMap")
	@RequiresUser
	public Map<Technique, Profile> profileMap() {
		return this.serviceManager.getBean(ProfileService.class)
				.buildProfileMap(this.getCurrentUser());
	}

	/**
	 * Add technique map to the service.
	 *
	 * <p>Techniques will be filtered based on the dossier and tier.</p>
	 *
	 * TODO move to service?
	 */
	@ModelAttribute("techniquesMap")
	public Map<Technique, String> techniques(@ModelAttribute Dossier dossier,
			@PathVariable Optional<String> tier,
			@ModelAttribute Method method) {

		if (dossier.getId() == 0) {
			return null;
		}

		List<Material> materials = this.serviceManager.getBean(MaterialTransactionalService.class)
				.loadNotArchivedDossierMaterials(dossier);

		Map<String, String> finalTechniqueSuitability = this.serviceManager.getBean(
				MaterialService.class).createTechniqueSuitabilityMap(materials);

		// When editing a method, extract the tier from the method instead of the path
		if (!tier.isPresent() && method.getId() != 0) {
			tier = Optional.of(new Tier(method.getTier()).getBaseTier());
		}

		// Filter techniques based on the dossier
		List<Technique> techniques;
		techniques = this.filterTechniquesForDossier(dossier);

		Map<Technique, String> techniquesMap = new HashMap<>();
		Set<String> tiers = null;
		// Filter techniques based on the tier
		for (Technique t : techniques) {
			tiers = this.serviceManager.getBean(TechniqueService.class).getTiers(t);
			if (finalTechniqueSuitability.containsKey(t.getSignifier())
					&& (!tier.isPresent() || tiers == null
					|| tiers.contains(tier.get()) || tiers.contains(tier.get() + "_na"))) {
				techniquesMap.put(t, finalTechniqueSuitability.get(t.getSignifier()));
			}
		}

		return techniquesMap;
	}

	/**
	 * Add unavailable techniques to the model.
	 *
	 * TODO add only where needed
	 *
	 * @see TechniqueService#loadUnavailableTechniquesForUser(User)
	 */
	@ModelAttribute("unavailableTechniques")
	@RequiresUser
	public List<Technique> unavailableTechniques() {
		return this.serviceManager.getBean(TechniqueService.class)
				.loadUnavailableTechniquesForUser(this.getCurrentUser());
	}
}
