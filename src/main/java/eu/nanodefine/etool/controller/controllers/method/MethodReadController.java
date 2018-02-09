/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Profile;
import eu.nanodefine.etool.model.repositories.ProfileRepository;
import eu.nanodefine.etool.model.services.dossier.DossierService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.model.services.view.TemplateService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for method overview.
 */
@Controller
@RequestMapping(value = "/" + Entities.METHOD + "/" + Actions.READ + "/"
		+ Entities.METHOD + ".id={methodId}")
public class MethodReadController extends AbstractMethodController {

	/**
	 * Shows the method overview.
	 *
	 * TODO validation
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Method method) {
		this.validateUserAwareEntities(method);

		return Templates.METHOD_READ;
	}

	/**
	 * Download of the method analysis file.
	 *
	 * TODO validation + only for methods which actually have analysis files
	 */
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@RequiresUser
	@ResponseBody
	public FileSystemResource getDownload(@ModelAttribute Method method,
			HttpServletResponse response) {

		this.validateUserAwareEntities(method);

		String fileName = this.methodService.extractOriginalDataFileName(method);

		// TODO we need to ensure that dossier and method names do not contain quotes
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		return new FileSystemResource(this.methodService.getMethodDataFilePath(method).toFile());
	}

	/**
	 * Download created plots.
	 *
	 * TODO validation + only for methods which actually have plots
	 */
	@GetMapping(value = "/plot={plotType}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@RequiresUser
	@ResponseBody
	public FileSystemResource getPlot(@ModelAttribute Method method, @PathVariable String plotType,
			HttpServletResponse response) {

		this.validateUserAwareEntities(method);

		List<String> plotTypes = ImmutableList.of("distribution", "density");

		if (!plotTypes.contains(plotType)) {
			plotType = "distribution";
		}

		String fileName = this.methodService.extractOriginalDataFileName(method)
				+ "." + plotType + ".png";
		response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
		return new FileSystemResource(
				this.methodService.getMethodDataFilePath(method) + "." + plotType + ".png");
	}

	/**
	 * Add method actions to the model.
	 */
	@ModelAttribute
	public void methodActionList(@ModelAttribute Method method,
			@ModelAttribute("actionList") List<ActionListEntry> actionList,
			@ModelAttribute("methodState") Integer methodState) {

		// TODO Bail out if method id == 0?
		Dossier dossier = method.getDossier();
		assert dossier != null; // TODO make sure dossier != null

		// TODO move to service
		boolean methodHasResult = method.getId() != 0 && method.hasResult();

		// TODO show but disable most actions if method is archived?
		if (!method.isArchived() && !dossier.isArchived()) {
			String update = this.uriService.builder(Entities.METHOD, Actions.UPDATE)
					.addEntityId(method).build();

			String archiveRedirect =
					this.uriService.builder(Entities.DOSSIER, Actions.READ)
							.addEntityId(dossier).setAnchor("methods").build();
			String archive = this.uriService.builder(Entities.METHOD, Actions.ARCHIVE)
					.addEntityId(method).addRedirectParam(archiveRedirect).build();

			actionList.add(new ActionListEntry(update, "method.read.method_update", "update"));
			actionList.add(new ActionListEntry(archive, "method.read.method_archive", "archive"));

			actionList.addAll(this.customAttributeActionList(method));

			String generateReport =
					this.uriService.builder(Entities.REPORT, Actions.CREATE).addEntityId(dossier)
							.addQueryParam(Entities.METHOD + "Ids", method.getId()).build();

			actionList.add(new ActionListEntry(generateReport, "method.read.report_generate",
					methodHasResult));
		}

		// Add method create actions
		if (!dossier.isArchived()) {
			actionList.addAll(this.methodService.buildMethodCreateActionList(dossier, true));
		}

		// Add plot downloads
		// TODO move check for data format to service
		// TODO split into analysis file download and plot download
		if (method.getDataFormat() != null
				&& (method.getDataFormat().equals(AnalysisDataFormats.PARTICLE_SIZER)
				|| method.getDataFormat().equals(AnalysisDataFormats.RIKILT_SPCTv2))) {

			String file = this.uriService.builder(Entities.METHOD, Actions.READ)
					.addEntityId(method).addPathParam("download").build();

			String distribution = this.uriService.builder(Entities.METHOD, Actions.READ)
					.addEntityId(method).addPathParam("plot", "distribution").build();

			String density = this.uriService.builder(Entities.METHOD, Actions.READ)
					.addEntityId(method).addPathParam("plot", "density").build();

			String fileSize = this.serviceManager.getBean(TemplateService.class)
					.generateFileSize(method.getDataFile());
			actionList.add(
					new ActionListEntry(file, "method.read.download.file", null,
							methodState >= MethodStates.ANALYSIS_DATA,
							null,
							new Object[] { fileSize }));
			actionList.add(new ActionListEntry(distribution, "method.read.download.distribution",
					methodState >= MethodStates.INTERMEDIATE_RESULTS));
			actionList.add(new ActionListEntry(density, "method.read.download.density",
					methodState >= MethodStates.INTERMEDIATE_RESULTS));
		}
	}

	/**
	 * Add the recommended action to the model.
	 *
	 * <p>This is added to highlight tier 2 method creation in case the result of this method was
	 * borderline and the method is tier 1.</p>
	 */
	@ModelAttribute
	protected void recommendedAction(@ModelAttribute Dossier dossier, Model model) {
		model.addAttribute("recommendedAction", this.serviceManager.getBean(DossierService.class)
				.determineRecommendedAction(dossier));
	}

	/**
	 * Reset method results.
	 *
	 * <p>Currently not publicly available, for debugging / development.</p>
	 */
	@GetMapping(value = "/reset")
	@RequiresUser
	public String reset(@ModelAttribute Method method) throws IOException {
		// TODO handle exception
		this.validateUserAwareEntities(method);
		this.methodService.resetMethodData(method);

		return this.buildRedirect(method);
	}

	/**
	 * Add default technique uncertainty to the model.
	 */
	@ModelAttribute("techniqueUncertainty")
	@RequiresUser
	public Double techniqueUncertainty(@ModelAttribute Method method) {
		return this.repositoryManager.getBean(ProfileRepository.class)
				.findByUserAndTechnique(this.getCurrentUser(), method.getTechnique())
				.orElse(new Profile(null, null, true, 0., 0., 0.)).getUncertainty();
	}
}
