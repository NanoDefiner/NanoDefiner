/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredTechnique;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;

/**
 * Service for configuration classes.
 *
 * This service contains methods which need to be available during application start-up and prior
 * to bean creation. The class is not tagged as {@link IService} to avoid
 * being picked up by the {@link eu.nanodefine.etool.model.helpers.managers.ServiceManager} and
 * creating cyclic dependencies.
 */
@Service
@Transactional
public class ConfigService {

	@Autowired
	private TechniqueRepository techniqueRepository;

	/**
	 * Updates the techniques in the database using the techniques in the KB.
	 */
	public List<Technique> updateTechniques(List<ConfiguredTechnique> entries) {
		Technique tPersisted;
		List<Technique> techniques = new ArrayList<>(entries.size());

		for (ConfiguredTechnique t : entries) {
			Optional<Technique> techniqueOptional =
					this.techniqueRepository.findFirstBySignifier(t.getSignifier());

			if (!techniqueOptional.isPresent()) {
				tPersisted = new Technique(t.getSignifier(), t.getName(), t.getComment());
			} else {
				tPersisted = techniqueOptional.get();
				tPersisted.setName(t.getName());
				tPersisted.setComment(t.getComment());
			}

			techniques.add(this.techniqueRepository.save(tPersisted));
		}

		return techniques;
	}
}
