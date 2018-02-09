/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.dossier;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.RepositoryManager;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.DossierRepository;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;

/**
 * Service for dossier-related processing.
 */
@Service
@Transactional
public class DossierService implements IService {

	@Autowired
	private DossierRepository dossierRepository;

	private Logger log = LoggerFactory.getLogger(DossierService.class);

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private ServiceManager serviceManager;

	/**
	 * Archives the given dossier.
	 */
	private void archiveDossier(Dossier dossier) {
		dossier.setArchived(true);
	}

	/**
	 * Archives the user dossiers with the given IDs.
	 *
	 * <p>Any dossier not belonging to the user will not be archived.</p>
	 */
	public void archiveUserDossiersByIds(User user, Integer[] dossierIds) {
		this.dossierRepository.findByUserAndIdIn(user, dossierIds).forEach(this::archiveDossier);
	}

	/**
	 * Determines the recommended action for the given dossier.
	 *
	 * Returns a locale string belonging to the recommended action or an empty string if no action
	 * is recommended (e.g. dossier is finished).
	 *
	 * TODO how to identify actions?
	 */
	public String determineRecommendedAction(Dossier dossier) {
		// Recommend material creation if no non-archived materials exist
		if (this.serviceManager.getBean(MaterialTransactionalService.class)
				.loadNotArchivedDossierMaterials(dossier).isEmpty()) {
			return "dossier.read.material_create";
		}

		MethodService ms = this.serviceManager.getBean(MethodService.class);

		// Recommend method creation if no non-archived methods exist
		if (ms.loadNotArchivedDossierMethods(dossier).isEmpty()) {
			return "dossier.read.method_create.tier1";
		}

		// Recommend tier 2 method creation if we have borderline results
		if (!ms.hasNonBorderlineResult(dossier)) {
			return "dossier.read.method_create.tier2";
		}

		// Recommend report creation if no non-archived reports exist and at least
		// one method has been finished
		if (ms.hasMethodsWithDataFile(dossier) &&
				this.serviceManager.getBean(ReportService.class)
						.loadNotArchivedDossierReports(dossier).isEmpty()) {
			return "dossier.read.report_create";
		}

		// No recommended action otherwise
		return "";
	}

	/**
	 * Determines the tab most likely to be interesting to the user for the given dossier.
	 *
	 * <ul>
	 *   <li>If there are no non-archived materials, show the material tab</li>
	 *   <li>If there are non-archived reports, show the report page</li>
	 *   <li>Otherwise, show the methods page</li>
	 * </ul>
	 */
	public String determineTab(Dossier dossier) {

		if (this.serviceManager.getBean(MaterialTransactionalService.class)
				.loadNotArchivedDossierMaterials(dossier).isEmpty()) {
			return "materials";
		}

		if (!this.serviceManager.getBean(ReportService.class)
				.loadNotArchivedDossierReports(dossier).isEmpty()) {
			return "reports";
		}

		return "methods";
	}

	/**
	 * Returns whether the dossier is considered finished.
	 *
	 * <p>A dossier is considered finished when there are non-archived reports.</p>
	 */
	public boolean isDossierFinished(Dossier dossier) {
		return !this.serviceManager.getBean(ReportService.class)
				.loadNotArchivedDossierReports(dossier).isEmpty();
	}

	/**
	 * Loads all user dossiers.
	 */
	public List<Dossier> loadUserDossiers(User user) {
		return this.dossierRepository.findByUser(user);
	}

	/**
	 * Loads user dossiers with given archived state.
	 */
	public List<Dossier> loadUserDossiers(User user, boolean archived) {
		return this.dossierRepository.findByUserAndArchived(user, archived);
	}

	/**
	 * Persists the given dossier for the given user.
	 */
	public Dossier persistDossier(Dossier dossier, User user) {
		dossier.setUser(user);

		return this.dossierRepository.save(dossier);
	}

	/**
	 * Saves the given dossier.
	 *
	 * TODO figure out when we need explicit saving
	 */
	public Dossier updateDossier(Dossier dossier) {
		return this.dossierRepository.save(dossier);
	}
}
