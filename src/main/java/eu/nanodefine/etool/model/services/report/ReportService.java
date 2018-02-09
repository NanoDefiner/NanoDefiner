/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.report;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.RepositoryManager;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.repositories.ReportRepository;
import eu.nanodefine.etool.model.services.ArchivableService;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Service for report-related processing.
 *
 * TODO split into methods which need @Transactional and those which don't
 */
@Service
public class ReportService implements IService {

	private final ArchivableService archivableService;

	private final Path knowledgeBaseArchiveLocation;

	private final ReportRepository reportRepository;

	private final RepositoryManager repositoryManager;

	private final ServiceManager serviceManager;

	private Logger log = LoggerFactory.getLogger(ReportService.class);

	@Autowired
	public ReportService(ArchivableService archivableService,
			@Qualifier("knowledgeBaseArchivePath") Path knowledgeBaseArchiveLocation,
			PerformanceDictionary performanceDictionary, RepositoryManager repositoryManager,
			ReportRepository reportRepository, ServiceManager serviceManager) {
		this.archivableService = archivableService;
		this.knowledgeBaseArchiveLocation = knowledgeBaseArchiveLocation;
		this.repositoryManager = repositoryManager;
		this.reportRepository = reportRepository;
		this.serviceManager = serviceManager;
	}

	/**
	 * Archive the reports belonging to the given entity.
	 *
	 * <p>Archival of reports happens when the given entity was changed. If the entity is a dossier
	 * or material, all reports of the associated dossier are archived. If the entity is a
	 * method, however, only reports which incorporate this method are archived.</p>
	 */
	@Transactional
	public void archiveCustomAttributeEntityReports(ICustomAttributeEntity entity) {
		switch (entity.getEntityType()) {
			case Entities.DOSSIER:
				Dossier dossier = (Dossier) entity;
				this.archiveDossierReports(dossier);
				break;
			case Entities.MATERIAL:
				Material material = (Material) entity;
				this.archiveDossierReports(material.getDossier());
				break;
			case Entities.METHOD:
				Method method = (Method) entity;
				this.archiveMethodReports(method);
				break;
			default:
				this.log.warn("Unknown custom attribute entity type: {}", entity.getEntityType());
		}
	}

	/**
	 * Archive reports of the given dossier.
	 */
	@Transactional
	public void archiveDossierReports(Dossier dossier) {
		this.archivableService.archiveEntities(
				this.reportRepository.findByDossierAndArchivedFalse(dossier));
	}

	/**
	 * Archive reports which incorporate the given method.
	 */
	@Transactional
	public void archiveMethodReports(Method method) {
		Set<Report> reports = method.getDossier().getReports();
		List<Report> reportsToBeArchived = new ArrayList<>();

		for (Report report : reports) {

			// If the report is not yet archived and incorporates the given method
			if (!report.isArchived() &&
					Arrays.asList(this.extractMethodIdsFromReport(report)).contains(method.getId())) {
				reportsToBeArchived.add(report);
			}
		}

		this.archivableService.archiveEntities(reportsToBeArchived);
	}

	/**
	 * Archive methods with the given IDs belonging to the given user.
	 */
	@Transactional
	public void archiveUserMethodsByIds(User currentUser, Integer[] reportIds) {
		Iterable<Report> reports = this.reportRepository.findByDossierUserAndIdIn(currentUser, reportIds);

		this.serviceManager.getBean(ArchivableService.class).archiveEntities(reports);
	}

	/**
	 * Creates and saves a PDF report for the given {@link Report} and method IDs.
	 *
	 * <p>Wrapper for {@link IPdfReportService#createAndWritePdfReport(Report, List)}.</p>
	 */
	public Report createAndSavePdfReport(Report report, String[] methodIds)
			throws Exception {
		report.setReportFile(this.determineReportFileName(report, methodIds));

		this.serviceManager.getBean(DynamicReportService.class)
				.createAndWritePdfReport(report, this.extractMethods(report));

		return report;
	}

	/**
	 * Determines the PDF file name for the given report and method IDs.
	 *
	 * <p>Since there is no direct relation between report and method entities, the IDs of the methods
	 * used in a report are stored in the report PDF file name.</p>
	 *
	 * @see #extractMethodIdsFromReport(Report)
	 */
	private String determineReportFileName(Report report, String[] methodIds) {

		FileService fs = this.serviceManager.getBean(FileService.class);

		return report.getDossier().getUser().getId() + "." +
				report.getDossier().getId() + "."
				+ String.join(",", methodIds) + ".report."
				+ fs.formatDateForFileName(new Date()) + ".pdf";
	}

	/**
	 * Returns a map of report-related files available for PDF-embedding.
	 *
	 * <p>These files include method analysis data, as well as custom attribute files.</p>
	 *
	 * <p>The keys are the file names (arbitrary information may be stored in the
	 * actual file names, in addition to the original file name) and the values
	 * the actual {@link File}s.</p>
	 */
	public Map<String, File> extractEmbeddableFiles(Report report) {
		Map<String, File> fileMap = new HashMap<>();

		String originalFileName;
		Dossier dossier = report.getDossier();
		Collection<Material> materials = dossier.getMaterials();
		Collection<Method> methods = this.extractMethods(report);
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		for (Method method : methods) {

			originalFileName = ms.extractOriginalDataFileName(method);

			if (originalFileName.equals("")) {
				continue;
			}

			fileMap.put(originalFileName, ms.getMethodDataFilePath(method).toFile());
		}

		// Custom attributes
		Collection<CustomAttribute> customAttributes = new ArrayList<>();
		customAttributes.addAll(dossier.getCustomAttributes());
		materials.forEach(m -> customAttributes.addAll(m.getCustomAttributes()));
		methods.forEach(m -> customAttributes.addAll(m.getCustomAttributes()));

		FileService fs = this.serviceManager.getBean(FileService.class);
		// TODO sort
		for (CustomAttribute customAttribute : customAttributes) {
			if (customAttribute.isFile()) {
				fileMap.put(fs.extractOriginalFileName(customAttribute.getValue()),
						fs.getFullPathInAnalysisDirectory(customAttribute.getValue()).toFile());
			}
		}

		// Knowledge base
		// TODO remove condition once KB published
		if (false) {
			File knowledgeBaseFile = this.knowledgeBaseArchiveLocation.toFile();
			fileMap.put(knowledgeBaseFile.getName(), knowledgeBaseFile);
		}

		return fileMap;
	}

	/**
	 * Extracts method IDs from the PDF filename of the given report.
	 *
	 * @see #determineReportFileName(Report, String[])
	 */
	public Integer[] extractMethodIdsFromReport(Report report) {
		String[] parts = report.getReportFile().split("\\.", 4);

		return Arrays.stream(parts[2].split(",")).mapToInt(Integer::valueOf).boxed()
				.toArray(Integer[]::new);
	}

	/**
	 * Returns a list of methods which were used to create the report.
	 *
	 * @see #extractMethodIdsFromReport(Report)
	 */
	public List<Method> extractMethods(Report report) {
		Integer[] methodIds = this.extractMethodIdsFromReport(report);

		Iterable<Method> methods = this.repositoryManager
				.getBean(MethodRepository.class).findAll(Arrays.asList(methodIds));

		return Lists.newArrayList(methods);
	}

	/**
	 * Returns a string representation of the set of tiers the given report is
	 * based on.
	 *
	 * <p>Used in the report tables template.</p>
	 */
	public String extractTiers(Report report) {
		Set<String> tierSet = new HashSet<>();

		for (Method m : this.extractMethods(report)) {
			tierSet.addAll(ConfigurationUtil.toSet(m.getTier()));
		}

		return ConfigurationUtil.toSetString(tierSet);
	}

	/**
	 * Loads not archived dossier reports.
	 */
	@Transactional(readOnly = true)
	public List<Report> loadNotArchivedDossierReports(Dossier dossier) {
		return this.reportRepository.findByDossierAndArchivedFalse(dossier);
	}

	/**
	 * Loads not archived user reports.
	 */
	@Transactional(readOnly = true)
	public List<Report> loadNotArchivedUserReports(User user) {
		return this.reportRepository
				.findByDossierUserAndArchivedFalseAndDossierArchivedFalse(user);
	}

	/**
	 * Loads all user reports.
	 */
	@Transactional(readOnly = true)
	public List<Report> loadUserReports(User user) {
		return this.reportRepository.findByDossierUser(user);
	}
}
