/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Material}s.
 */
public interface MaterialRepository extends CrudRepository<Material, Integer> {

	/**
	 * Delete user template materials with the given IDs.
	 */
	void deleteByUserAndIdInAndTemplateTrue(User user, Integer... ids);

	/**
	 * Find dossier materials with given archived state.
	 */
	List<Material> findByDossierAndArchived(Dossier dossier,
			boolean archived);

	/**
	 * Find dossier materials with given archived state except the one with the given ID.
	 */
	List<Material> findByDossierAndArchivedAndIdNot(Dossier dossier, boolean archived,
			Integer id);

	/**
	 * Find user materials.
	 */
	List<Material> findByUser(User user);

	/**
	 * Find non-archived user materials that are not templates.
	 *
	 * TODO too long, query instead?
	 */
	List<Material> findByUserAndArchivedFalseAndDossierArchivedFalseAndTemplateFalse(
			User user);

	/**
	 * Find user materials with the given IDs.
	 */
	Iterable<Material> findByUserAndIdIn(User user, Integer... ids);

	/**
	 * Find user template materials.
	 */
	List<Material> findByUserAndTemplateTrue(User user);
}
