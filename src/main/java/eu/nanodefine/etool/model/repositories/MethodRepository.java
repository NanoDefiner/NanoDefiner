/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Method}s.
 */
public interface MethodRepository extends CrudRepository<Method, Integer> {

	/**
	 * Find dossier methods with given archived state.
	 */
	List<Method> findByDossierAndArchived(Dossier dossier, boolean archived);

	/**
	 * Find dossier methods with analysis results which are not archived.
	 *
	 * TODO well, this name is getting out of hand, maybe go for an explicit
	 * query here?
	 */
	List<Method> findByDossierAndDataFileIsNotNullAndArchivedFalse(
			Dossier dossier);

	/**
	 * Find dossier methods with given IDs.
	 */
	Iterable<Method> findByDossierAndIdIn(Dossier dossier, Integer[] ids);

	/**
	 * Find user methods.
	 */
	List<Method> findByDossierUser(User user);

	/**
	 * Find non-archived user methods.
	 */
	List<Method> findByDossierUserAndArchivedFalseAndDossierArchivedFalse(
			User user);

	Iterable<Method> findByDossierUserAndIdIn(User user, Integer[] ids);
}
