/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Profile}s.
 */
public interface ProfileRepository extends CrudRepository<Profile, Integer> {

	List<Profile> findByUser(User user);

	/**
	 * Find user technique profile.
	 */
	Optional<Profile> findByUserAndTechnique(User user,
			Technique technique);

	/**
	 * Find enabled user technique profile.
	 */
	Optional<Profile> findByUserAndTechniqueAndEnabledTrue(User user,
			Technique technique);
}
