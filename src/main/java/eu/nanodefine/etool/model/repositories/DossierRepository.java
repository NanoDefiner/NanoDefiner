/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Dossier}s.
 */
public interface DossierRepository extends CrudRepository<Dossier, Integer> {

	/**
	 * Count user dossiers.
	 */
	Integer countByUser(User user);

	/**
	 * Count archived user dossiers.
	 */
	Integer countByUserAndArchivedTrue(User user);

	/**
	 * Find user dossiers.
	 */
	List<Dossier> findByUser(User user);

	/**
	 * Find user dossier by archived state.
	 */
	List<Dossier> findByUserAndArchived(User user, boolean archived);

	/**
	 * Find user dossiers with given IDs.
	 */
	Collection<Dossier> findByUserAndIdIn(User user, Integer... ids);

	/**
	 * Find user dossier with given ID.
	 */
	Optional<Dossier> findFirstByIdAndUser(Integer id, User user);
}
