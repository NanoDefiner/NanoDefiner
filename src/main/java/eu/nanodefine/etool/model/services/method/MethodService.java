/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.MethodAnalysisServiceManager;
import eu.nanodefine.etool.model.helpers.managers.RepositoryManager;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;
import eu.nanodefine.etool.model.services.ArchivableService;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.method.analysis.IAnalysisDataFormatService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.model.services.report.ReportService;
import eu.nanodefine.etool.model.services.view.UriService;
import eu.nanodefine.etool.utilities.classes.UriBuilder;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Service for method-related processing.
 */
@Service
@Transactional
public class MethodService implements IService {

	protected final PerformanceConfiguration performanceConfiguration;

	private final MethodAnalysisServiceManager methodAnalysisServiceManager;

	private final MethodRepository methodRepository;

	private final RepositoryManager repositoryManager;

	private final ServiceManager serviceManager;

	private Logger log = LoggerFactory.getLogger(MethodService.class);

	@Autowired
	public MethodService(PerformanceConfiguration performanceConfiguration,
			MethodAnalysisServiceManager methodAnalysisServiceManager, MethodRepository methodRepository,
			RepositoryManager repositoryManager, ServiceManager serviceManager) {
		this.performanceConfiguration = performanceConfiguration;
		this.methodAnalysisServiceManager = methodAnalysisServiceManager;
		this.methodRepository = methodRepository;
		this.repositoryManager = repositoryManager;
		this.serviceManager = serviceManager;
	}

	/**
	 * Add initial analysis data for the given method.
	 */
	public Method addMethodData(Method method, String data, Double uncertainty,
			String dataFormatString) throws Exception {

		assert method.getDataFormat() == null;
		method.setDataFile(data);
		method.setTechniqueUncertainty(uncertainty);

		// TODO deal with invalid data formats?
		IAnalysisDataFormatService methodProcessor = this.methodAnalysisServiceManager
				.getBean(dataFormatString);
		method.setDataFormat(methodProcessor.getAnalysisDataFormat());

		try {
			methodProcessor.update(method, data, uncertainty);
		} catch (Exception e) {
			// Reset method and re-throw
			this.resetMethodData(method);
			throw e;
		}

		return method;
	}

	/**
	 * Archive all methods of the given dossier.
	 */
	public void archiveDossierMethods(Dossier dossier) {
		this.serviceManager.getBean(ArchivableService.class).archiveEntities(
				this.methodRepository.findByDossierAndArchived(dossier, false));
	}

	/**
	 * Archive methods with the given IDs of the given user.
	 */
	public void archiveUserMethodsByIds(User user, Integer[] methodIds) {
		Iterable<Method> methods = this.methodRepository.findByDossierUserAndIdIn(user, methodIds);

		ReportService rs = this.serviceManager.getBean(ReportService.class);

		methods.forEach(rs::archiveMethodReports);

		this.serviceManager.getBean(ArchivableService.class).archiveEntities(methods);
	}

	/**
	 * Builds the action list for method creation.
	 *
	 * TODO move to template service
	 */
	public List<ActionListEntry> buildMethodCreateActionList(Dossier dossier, boolean enabled) {

		UriBuilder createMethodUri = this.serviceManager.getBean(UriService.class)
				.builder(Entities.METHOD, Actions.CREATE).addEntityId(dossier);

		String localeString = "dossier.read.method_create.";
		String tierParam = Entities.METHOD + "." + PerformanceAttributes.TIER;
		String[] tierValues = new String[] { "tier1", "tier2" };
		String[] createMethodPaths = new String[] {
				createMethodUri.copy().addPathParam(tierParam, tierValues[0]).build(),
				createMethodUri.copy().addPathParam(tierParam, tierValues[1]).build(),
		};

		boolean tier1Enabled =
				!this.hasMethodsWithDataFile(dossier) || this.hasNonBorderlineResult(dossier);

		return ImmutableList.of(new ActionListEntry(createMethodPaths[0],
						localeString + tierValues[0], enabled && tier1Enabled),
				new ActionListEntry(createMethodPaths[1], localeString + tierValues[1],
						enabled));
	}

	/**
	 * Convenience method for determining the state of a method.
	 *
	 * @see MethodStates
	 */
	public Integer determineState(Method method) {
		return this.methodAnalysisServiceManager.getBean(method.getDataFormat()).determineState(method);
	}

	/**
	 * Simple switch between "tier1" and "tier2", whichever is given will not be returned.
	 */
	public String determineTierSwitch(String tier) {
		return (tier.equals("tier1") ? "tier2" : "tier1");
	}

	/**
	 * Extracts the original file name from a method analysis file.
	 */
	public String extractOriginalDataFileName(Method method) {
		return this.hasAnalysisFile(method) ? this.serviceManager.getBean(FileService.class)
				.extractOriginalFileName(method.getDataFile()) : "";
	}

	/**
	 * Returns a string representation of the method analysis file.
	 */
	private String getDataFileString(Method method) {
		return this.hasAnalysisFile(method) ?
				this.getMethodDataFilePath(method).toString() :
				method.getDataFile();
	}

	/**
	 * Returns the {@link Path} of the given method's data file, or null if none is available.
	 */
	public Path getMethodDataFilePath(Method m) {
		if (m.getDataFile() == null || !this.hasAnalysisFile(m)) {
			return null;
		}

		String fileName = m.getDataFile();

		return this.serviceManager.getBean(FileService.class).getFullPathInAnalysisDirectory(fileName);
	}

	/**
	 * Returns whether the given method supports an analysis file as analysis result.
	 */
	public boolean hasAnalysisFile(Method method) {
		switch (method.getDataFormat()) {
			case AnalysisDataFormats.PARTICLE_SIZER:
			case AnalysisDataFormats.RIKILT_SPCTv2:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns whether the given dossier has methods with data files.
	 */
	public boolean hasMethodsWithDataFile(Dossier dossier) {
		// TODO add count query method?
		return !this.methodRepository.findByDossierAndDataFileIsNotNullAndArchivedFalse(dossier)
				.isEmpty();
	}

	/**
	 * Returns whether the given dossier has methods with non-borderline results.
	 */
	public boolean hasNonBorderlineResult(Dossier dossier) {
		for (Method m : dossier.getMethods()) {
			if (!m.isArchived() && m.hasResult() && !this.isBorderline(m)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the given dossier has not archived methods without results.
	 */
	public boolean hasNotArchivedMethodsWithoutResults(Dossier dossier) {
		for (Method m : dossier.getMethods()) {
			if (!m.isArchived() && !m.hasResult()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the given method has a borderline result.
	 */
	public boolean isBorderline(Method method) {
		Double value = method.hasResult() ?
				Double.valueOf(method.getResult()) : 0d;

		// TODO extract from properties / knowledge base
		return method.getTier().contains("tier1") && value >= 100 && value < 250;
	}

	/**
	 * Returns whether the given method is considered finished.
	 *
	 * @see MethodStates#FINISHED
	 */
	public boolean isFinished(Method method) {
		return this.determineState(method) == MethodStates.FINISHED;
	}

	/**
	 * Returns whether the given method has a nano result.
	 */
	public boolean isNano(Method method) {

		Double result = method.hasResult() ? Double.valueOf(method.getResult()) : 101;

		return result <= 100;
	}

	/**
	 * Loads not archived methods of the given dossier.
	 */
	@Transactional(readOnly = true)
	public List<Method> loadNotArchivedDossierMethods(Dossier dossier) {
		return this.methodRepository.findByDossierAndArchived(dossier, false);
	}

	/**
	 * Loads methods for the given dossier which have anaylsis results.
	 */
	@Transactional(readOnly = true)
	public List<Method> loadNotArchivedDossierMethodsWithDataFile(
			Dossier dossier) {
		return this.methodRepository
				.findByDossierAndDataFileIsNotNullAndArchivedFalse(dossier);
	}

	/**
	 * Loads not archived methods of the given user.
	 */
	@Transactional(readOnly = true)
	public List<Method> loadNotArchivedUserMethods(User user) {
		return this.methodRepository
				.findByDossierUserAndArchivedFalseAndDossierArchivedFalse(user);
	}

	/**
	 * Loads methods included in the given report.
	 *
	 * <p>Since there is no direct relation between reports and methods, the methods included in a
	 * report are encoded in the report file name.</p>
	 */
	@Transactional(readOnly = true)
	public List<Method> loadReportMethods(Report report) {
		return Lists.newArrayList(this.methodRepository.findAll(
				Arrays.asList(this.serviceManager.getBean(ReportService.class)
						.extractMethodIdsFromReport(report))));

	}

	/**
	 * Loads all methods of the given user.
	 */
	@Transactional(readOnly = true)
	public List<Method> loadUserMethods(User user) {
		return this.methodRepository.findByDossierUser(user);
	}

	/**
	 * Persists the given method of the given dossier.
	 */
	public Method persistMethod(Dossier dossier, Method methodForm) {
		Technique technique = this.repositoryManager.getBean(TechniqueRepository.class)
				.findOne(methodForm.getTechnique().getId());

		methodForm.setDossier(dossier);
		methodForm.setTier(this.performanceConfiguration.getEntry(technique.getSignifier())
				.getEntry(PerformanceAttributes.TIER).getValue());
		methodForm.setTechniqueUncertainty(0.);

		return this.methodRepository.save(methodForm);
	}

	/**
	 * Registers the given result for the given method.
	 */
	public void registerResult(Method method, Double result) {
		method.setResult(result != null ? String.format("%.2f", result) : null);
	}

	/**
	 * Resets the given method's state.
	 *
	 * TODO BET
	 */
	public Method resetMethodData(Method method) throws IOException {

		Path filePath = this.getMethodDataFilePath(method);

		// TODO duplicate code
		if (filePath != null) {
			Files.deleteIfExists(filePath);
			Files.deleteIfExists(Paths.get(filePath.toString() + ".distribution.png"));
			Files.deleteIfExists(Paths.get(filePath.toString() + ".density.png"));
		}

		method.setResult(null);
		method.setDataFile(null);
		method.setDataFormat(null);
		method.getAttributes().clear();

		return method;
	}

	/**
	 * Updates the given method and archives associated reports.
	 */
	public Method updateMethod(Method method) {
		Method methodPersisted = this.methodRepository.save(method);

		// Archive associated reports
		this.serviceManager.getBean(ReportService.class).archiveMethodReports(methodPersisted);

		return methodPersisted;
	}

	/**
	 * Updates the analysis data for the given method.
	 */
	public Method updateMethodData(Method method, String data) throws Exception {
		assert method.getDataFormat() != null;

		IAnalysisDataFormatService methodProcessor = this.methodAnalysisServiceManager
				.getBean(method.getDataFormat());

		assert methodProcessor != null;

		methodProcessor.update(method, data);

		return this.methodRepository.save(method);
	}

	/**
	 * Writes the given file to the given path.
	 *
	 * TODO move to FileService
	 */
	public boolean writeAnalysisFile(Path filePath, MultipartFile file) {
		try {
			Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			// TODO Display error to user
			this.log.error("Unable to write analysis file to disk:", e);

			return false;
		}

		return true;
	}
}
