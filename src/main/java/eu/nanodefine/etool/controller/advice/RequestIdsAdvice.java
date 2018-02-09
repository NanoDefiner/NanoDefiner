/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.advice;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Issue;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.managers.RepositoryManager;
import eu.nanodefine.etool.model.repositories.DossierRepository;
import eu.nanodefine.etool.model.repositories.IssueRepository;
import eu.nanodefine.etool.model.repositories.MaterialRepository;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.repositories.ReportRepository;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;

/**
 * Injects objects into the model the IDs of which were passed via the request
 * URI.
 *
 * <p>For IDs that were not passed or are set to 0, new objects are created and
 * injected into the model. The IDs of these objects are set to 0 to allow safe
 * creation of URIs.</p>
 *
 * <p>Entity IDs are extracted as</p>
 *
 * <ul>
 * <li><em>path variables</em>, e.g. {@code /entity/action/dossier.id=1},</li>
 * <li><em>request parameters</em>, e.g. {@code /entity/action?dossier.id=1}</li>
 * </ul>
 */
@ControllerAdvice
public class RequestIdsAdvice {

	private final RepositoryManager repositoryManager;

	private Logger log = LoggerFactory.getLogger(RequestIdsAdvice.class);

	@Autowired
	public RequestIdsAdvice(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	/**
	 * Extracts {@link Dossier} entity from the request.
	 *
	 * <p>The dossier can be extracted directly from the {@code dossierId} request
	 * parameter or indirectly through the {@link Material}, {@link Method}, or
	 * {@link Report} entity.</p>
	 *
	 * @param dossierIdPath Dossier ID path variable, if available
	 * @param material {@link Material} entity as determined by
	 * {@link #material(Optional, Optional)}.
	 * @param method {@link Method} entity as determined by
	 * {@link #method(Optional, Optional)}.
	 * @param report {@link Report} entity as determined by
	 * {@link #report(Optional, Optional)}.
	 * @return {@link Dossier} entity associated with this request or an empty
	 * dossier if no dossier ID could be found
	 */
	@ModelAttribute(binding = false)
	public Dossier dossier(@PathVariable("dossierId") Optional<Integer> dossierIdPath,
			@RequestParam(name = Entities.DOSSIER + ".id") Optional<Integer> dossierIdRequest,
			@ModelAttribute Material material, @ModelAttribute Method method,
			@ModelAttribute Report report) {
		Dossier dossier = null;

		// 0 is valid, it means the request is not associated with a dossier
		Integer dossierId = dossierIdPath.orElse(dossierIdRequest.orElse(-1));

		// Dossier ID directly from the request
		if (dossierId != -1) {
			dossier = this.repositoryManager.getBean(DossierRepository.class)
					.findOne(dossierId);
		} else if (report.getId() != 0) { // Via report
			dossier = report.getDossier();
		} else if (method.getId() != 0) { // Via method
			dossier = method.getDossier();
		} else if (material.getId() != 0 && !material.isTemplate()) { // Via material
			dossier = material.getDossier();
		}

		// Create empty dossier if none was found above
		if (dossier == null) {
			dossier = new Dossier();
			dossier.setId(0);
		}

		return dossier;
	}

	/**
	 * Extracts an {@link Issue} entity from the request.
	 *
	 * <p>If an issue ID was successfully extracted from the request, it is loaded
	 * from the database. If an invalid or no ID was provided, an empty issue is
	 * created.</p>
	 *
	 * @param issueIdPath Issue ID path variable, if available
	 * @param issueIdRequest Issue ID request parameter, if available
	 */
	@ModelAttribute(binding = false)
	public Issue issue(@PathVariable("issueId") Optional<Integer> issueIdPath,
			@RequestParam(name = Entities.ISSUE + ".id") Optional<Integer> issueIdRequest) {

		Issue issue = null;
		Integer issueId = issueIdPath.orElse(issueIdRequest.orElse(0));

		if (issueId != 0) {
			issue = this.repositoryManager.getBean(IssueRepository.class).findOne(issueId);
		}

		if (issue == null) {
			issue = new Issue();
			issue.setId(0);
		}

		return issue;
	}

	/**
	 * Extracts a material entity from the request.
	 *
	 * <p>If a material ID was successfully extracted from the request, it is loaded
	 * from the database. If an invalid or no ID was provided, an empty material
	 * is created.</p>
	 *
	 * <p>We use {@link String} here to avoid exceptions when non-integer values are
	 * provided, like for the reference material signifiers used during material
	 * creation.</p>
	 *
	 * TODO Handle reference signifiers separately to keep the code cleaner here?
	 *
	 * @param materialIdPath Material ID path variable or signifier, if available
	 * @param materialIdRequest Material ID request parameter or signifier, if available
	 */
	@ModelAttribute(binding = false)
	public Material material(@PathVariable("materialId") Optional<String> materialIdPath,
			@RequestParam(name = Entities.MATERIAL + ".id") Optional<String> materialIdRequest) {

		Material material = null;
		String materialId = materialIdPath.orElse(materialIdRequest.orElse(null));

		if (materialId != null) {
			try {
				material = this.repositoryManager.getBean(MaterialRepository.class)
						.findOne(Integer.valueOf(materialId));
			} catch (NumberFormatException e) {
				material = null;
			}
		}

		if (material == null) {
			material = new Material();
			material.setId(0);
		}

		return material;
	}

	/**
	 * Extracts a method from the request.
	 *
	 * <p>If a method ID was successfully extracted from the request, it is loaded
	 * from the database. If an invalid or no ID was provided, an empty method
	 * is created.</p>
	 *
	 * @param methodIdPath Method ID path variable, if available
	 * @param methodIdRequest Method ID request parameter, if available
	 */
	@ModelAttribute(binding = false)
	public Method method(@PathVariable("methodId") Optional<Integer> methodIdPath,
			@RequestParam(name = Entities.METHOD + ".id") Optional<Integer> methodIdRequest) {

		Method method = null;
		Integer methodId = methodIdPath.orElse(methodIdRequest.orElse(0));

		if (methodId != 0) {
			method = this.repositoryManager.getBean(MethodRepository.class)
					.findOne(methodId);
		}

		if (method == null) {
			method = new Method();
			method.setId(0);
			// Set to valid empty set
			// TODO solve cleaner, use default value?
			method.setPreparation("{}");
		}

		return method;
	}

	/**
	 * Extracts a report from the request.
	 *
	 * <p>If a report ID was successfully extracted from the request, it is loaded
	 * from the database. If an invalid or no ID was provided, an empty report
	 * is created.</p>
	 *
	 * @param reportIdPath Report ID path variable, if available
	 * @param reportIdRequest Report ID request parameter, if available
	 */
	@ModelAttribute(binding = false)
	public Report report(@PathVariable("reportId") Optional<Integer> reportIdPath,
			@RequestParam(name = Entities.REPORT + ".id") Optional<Integer> reportIdRequest) {

		Report report = null;
		Integer reportId = reportIdPath.orElse(reportIdRequest.orElse(0));

		if (reportId != 0) {
			report = this.repositoryManager.getBean(ReportRepository.class)
					.findOne(reportId);
		}

		if (report == null) {
			report = new Report();
			report.setId(0);
		}

		return report;
	}

	/**
	 * Extracts a technique from the request.
	 *
	 * <p>If a technique ID was successfully extracted from the request, it is loaded
	 * from the database. If an invalid or no ID was provided, an empty technique
	 * is created.</p>
	 *
	 * @param techniqueIdPath Technique ID path variable, if available
	 * @param techniqueIdRequest Technique ID request parameter, if available
	 */
	@ModelAttribute(binding = false)
	public Technique technique(@PathVariable("techniqueId") Optional<Integer> techniqueIdPath,
			@RequestParam(name = Entities.TECHNIQUE + ".id")
					Optional<Integer> techniqueIdRequest) {

		Technique technique = null;
		Integer techniqueId = techniqueIdPath.orElse(techniqueIdRequest.orElse(0));

		if (techniqueId != 0) {
			technique = this.repositoryManager.getBean(TechniqueRepository.class)
					.findOne(techniqueId);
		}

		if (technique == null) {
			technique = new Technique();
			technique.setId(0);
		}

		return technique;
	}

}
