/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.material;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.MaterialRepository;
import eu.nanodefine.etool.model.services.ArchivableService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Service for transactional material-related processing.
 */
@Service
@Transactional
public class MaterialTransactionalService implements IService {

	private final MaterialRepository materialRepository;

	private final PerformanceDictionary performanceDictionary;

	private final ServiceManager serviceManager;

	@Autowired
	public MaterialTransactionalService(MaterialRepository materialRepository,
			PerformanceDictionary performanceDictionary, ServiceManager serviceManager) {
		this.materialRepository = materialRepository;
		this.performanceDictionary = performanceDictionary;
		this.serviceManager = serviceManager;
	}

	/**
	 * Archives the given material.
	 *
	 * <p>Archiving a material also archives all associated dossier methods and reports.</p>
	 */
	public void archiveMaterial(Material material) {
		Dossier dossier = material.getDossier();
		material.setArchived(true);

		ArchivableService as = this.serviceManager.getBean(ArchivableService.class);

		as.archiveEntities(dossier.getMethods());
		as.archiveEntities(dossier.getReports());
	}

	/**
	 * Archives the materials with the given IDs of the given user.
	 *
	 * @see #archiveMaterial(Material)
	 */
	public void archiveUserMaterialsByIds(User user, Integer[] materialIds) {
		this.materialRepository.findByUserAndIdIn(user, materialIds).forEach(this::archiveMaterial);
	}

	/**
	 * Deletes the material templates with the given IDs of the given user.
	 */
	public void deleteUserTemplatesByIds(User user, Integer[] materialIds) {
		this.materialRepository.deleteByUserAndIdInAndTemplateTrue(user, materialIds);
	}

	/**
	 * Loads not archived materials of the given dossier.
	 */
	@Transactional(readOnly = true)
	public List<Material> loadNotArchivedDossierMaterials(Dossier dossier) {
		return this.materialRepository.findByDossierAndArchived(dossier, false);
	}

	/**
	 * Loads not archived materials of the given user.
	 */
	@Transactional(readOnly = true)
	public List<Material> loadNotArchivedUserMaterials(User user) {
		return this.materialRepository
				.findByUserAndArchivedFalseAndDossierArchivedFalseAndTemplateFalse(
						user);
	}

	/**
	 * Loads other not archived materials of the dossier associated with the given material.
	 */
	@Transactional(readOnly = true)
	public List<Material> loadOtherNotArchivedDossierMaterials(Material material) {
		return this.materialRepository
				.findByDossierAndArchivedAndIdNot(material.getDossier(), false, material.getId());
	}

	/**
	 * Loads all materials of the given user.
	 */
	@Transactional(readOnly = true)
	public List<Material> loadUserMaterials(User user) {
		return this.materialRepository.findByUser(user);
	}

	/**
	 * Loads material templates of the given user.
	 */
	@Transactional(readOnly = true)
	public List<Material> loadUserTemplateMaterials(User user) {
		return this.materialRepository.findByUserAndTemplateTrue(user);
	}

	/**
	 * Persists the given material and its material crtieria.
	 */
	public void persistMaterial(Material material,
			Set<MaterialCriterion> materialCriteria) {
		materialCriteria.forEach(mc -> mc.setMaterial(material));

		this.materialRepository.save(material);
	}

	/**
	 * Updates the given material from the given request parameters.
	 *
	 * <p>All methods and reports of the associated dossier are archived.</p>
	 */
	public void updateMaterialFromRequestParameters(Material material,
			Map<String, String[]> parameterMap) {

		// Update basic material attributes
		material.setName(parameterMap.get(Entities.MATERIAL + ".name")[0]);
		material.setComment(parameterMap.get(Entities.MATERIAL + ".comment")[0]);
		material.setSignifier(parameterMap.get(Entities.MATERIAL + "."
				+ PerformanceAttributes.MATERIAL_SIGNIFIER)[0]);

		// Update material criteria
		String[] values;
		Attribute a;
		MaterialCriterion mcForm;
		for (MaterialCriterion mc : material.getMaterialCriterions()) {
			values = parameterMap.get(Entities.MATERIAL + "." + mc.getName());

			if (values != null) {
				a = this.performanceDictionary.getAttribute(mc.getName());

				mcForm = ConfigurationUtil.createMaterialCriterion(values, a);
				mc.setValue(mcForm.getValue());
			}
		}

		// Archive reports
		this.serviceManager.getBean(ReportService.class)
				.archiveDossierReports(material.getDossier());

		// Archive methods
		this.serviceManager.getBean(MethodService.class)
				.archiveDossierMethods(material.getDossier());
	}
}
