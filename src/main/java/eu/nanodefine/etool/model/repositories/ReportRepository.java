/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link Report}s.
 */
public interface ReportRepository extends CrudRepository<Report, Integer> {

	/**
	 * Count user reports.
	 */
	Integer countByDossierUser(User user);

	/**
	 * Find non-archived dossier reports.
	 */
	List<Report> findByDossierAndArchivedFalse(Dossier dossier);

	/**
	 * Find user reports.
	 */
	List<Report> findByDossierUser(User user);

	/**
	 * Find non-archived user reports.
	 */
	List<Report> findByDossierUserAndArchivedFalseAndDossierArchivedFalse(User user);

	/**
	 * Find user reports with the given IDs.
	 */
	Iterable<Report> findByDossierUserAndIdIn(User currentUser, Integer[] reportIds);
}
