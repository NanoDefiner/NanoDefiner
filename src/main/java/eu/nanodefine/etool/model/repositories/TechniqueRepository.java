/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Technique}s.
 */
public interface TechniqueRepository
		extends CrudRepository<Technique, Integer> {

	/**
	 * Loads techniques for the given user that are not enabled.
	 */
	List<Technique> findByProfilesUserAndProfilesEnabledFalse(User user);

	/**
	 * Returns the first technique for the given signifier.
	 */
	Optional<Technique> findFirstBySignifier(String signifier);
}
