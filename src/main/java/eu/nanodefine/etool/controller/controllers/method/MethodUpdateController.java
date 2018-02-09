/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.helpers.method.Tier;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;
import eu.nanodefine.etool.model.services.method.constants.MethodAttributes;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.utilities.classes.UriBuilder;

/**
 * Controller for updating methods.
 */
@Controller
@RequestMapping("/" + Entities.METHOD + "/" + Actions.UPDATE + "/"
		+ Entities.METHOD + ".id={methodId}")
public class MethodUpdateController extends AbstractMethodController {

	/**
	 * Show method creation form with values pre-filled.
	 *
	 * <p>The technique selection page is skipped, it can not be changed.</p>
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute("methodForm") Method method, Model model) {

		this.validateUserAwareEntities(method);

		model.addAttribute("tier", new Tier(method.getTier()).getBaseTier());

		return Templates.METHOD_CREATE;
	}

	/**
	 * Persist method changes.
	 */
	@PostMapping
	@RequiresUser
	public String post(@ModelAttribute("methodForm") @Valid Method methodForm,
			BindingResult result, @ModelAttribute Dossier dossier, Model model) {

		this.validateUserAwareEntities(methodForm);

		Method methodPersisted = this.methodService.updateMethod(methodForm);

		model.addAttribute(methodForm);

		return UriBuilder.create(Entities.METHOD, Actions.READ)
				.addEntityId(methodPersisted).buildRedirect();
	}

	/**
	 * Analysis data upload for BET.
	 */
	@PostMapping(value = "/BET")
	@RequiresUser
	public String postBet(@RequestParam(Entities.METHOD + ".vssa") String vssa, Model model,
			@RequestParam(Entities.METHOD + ".technique_uncertainty") Double uncertainty,
			@ModelAttribute Method method, @RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(method);

		// TODO validate method state
		Method methodUpdated = method;
		try {
			methodUpdated = this.methodService.addMethodData(method, vssa, uncertainty,
					AnalysisDataFormats.VSSA);
		} catch (Exception e) {
			// TODO handle error
			this.log.error("Analysis data processing exception: {}", e.getMessage());
			this.log.debug("Exception:", e);
		}

		// If the result is null there was a problem with the uploaded data
		// TODO what are the different scenarios here, do we need different error messages?
		if (!methodUpdated.hasResult()) {
			errors.add("method.update.error.bet");
		}

		return this.buildRedirect(method);
	}

	/**
	 * Analysis data upload for manual D50.
	 */
	@PostMapping(value = "/D50")
	@RequiresUser
	public String postD50(@RequestParam(Entities.METHOD + ".D50") String d50, Model model,
			@RequestParam(Entities.METHOD + ".technique_uncertainty") Double uncertainty,
			@ModelAttribute Method method, @RequestAttribute("errors") List<String> errors) {
		this.validateUserAwareEntities(method);

		// TODO validate method state
		Method methodUpdated = method;
		try {
			methodUpdated = this.methodService
					.addMethodData(method, d50, uncertainty, AnalysisDataFormats.MANUAL_D50);
		} catch (Exception e) {
			// TODO handle error
		}

		// TODO can this even happen unless the user fiddles with the form on the client side?
		if (methodUpdated.getResult() == null) {
			errors.add("method.read.error.D50");
		}

		return this.buildRedirect(method);
	}

	/**
	 * Analysis data upload for using ParticleSizer export format.
	 */
	@PostMapping(value = "/particleSizer")
	@RequiresUser
	public String postParticleSizer(
			@RequestParam(Entities.METHOD + ".analysis_file") MultipartFile file,
			@RequestParam(Entities.METHOD + ".technique_uncertainty") Double uncertainty,
			@ModelAttribute Method method,
			@RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(method);

		// TODO extract duplicate code, sanitize original file name
		Path filePath = Paths.get(this.serviceManager.getBean(FileService.class)
				.buildFileNamePrefix(this.getCurrentUser(), method) + file.getOriginalFilename());

		String redirect = this.buildRedirect(method);

		if (!this.methodService.writeAnalysisFile(filePath, file)) {
			errors.add("method.update.error.analysis_file");
			return redirect;
		}

		// TODO move to service, clean up
		try {
			this.methodService
					.addMethodData(method, filePath.getFileName().toString(), uncertainty,
							AnalysisDataFormats.PARTICLE_SIZER);
		} catch (Exception e) {

			try {
				Files.deleteIfExists(filePath);
				Files.deleteIfExists(Paths.get(filePath.toString() + ".distribution.png"));
				Files.deleteIfExists(Paths.get(filePath.toString() + ".density.png"));
			} catch (IOException e1) {
				this.log.error("Unable to delete analysis file '{}' after failed processing: {}",
						filePath, e.getMessage());
			}

			errors.add("method.update.error.analysis_file");
		}

		return redirect;
	}

	/**
	 * Plausibility check.
	 */
	@PostMapping(value = "/plausibility")
	@RequiresUser
	public String postPlausibility(@RequestParam("size") Double d50, Model model,
			@ModelAttribute Method method, @ModelAttribute("methodState") Integer methodState,
			@RequestAttribute("successes") List<String> successes,
			@RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(method);
		String redirect = this.buildRedirect(method);

		if (!methodState.equals(MethodStates.SIZE_PLAUSIBILITY_CHECK)) {
			// TODO display error message?
			return redirect;
		}

		// TODO Validate method state
		Method methodUpdated = method;
		try {
			methodUpdated = this.methodService.updateMethodData(method, d50.toString());
		} catch (Exception e) {
			this.log.error("Exception during plausibiltiy check processing:", e);
			// TODO handle error?
		}

		if (this.methodService.isFinished(method) &&
				!method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_FAILED)) {
			successes.add("method.update.success.plausibility");

		} else {
			errors.add("method.update.error.plausibility");
		}

		return redirect;
	}

	/**
	 * Analysis data upload for SP-ICP-MS.
	 */
	@PostMapping(value = "/SPICPMS")
	@RequiresUser
	public String postSpicpms(@ModelAttribute Method method,
			@RequestParam(Entities.METHOD + ".analysis_file") MultipartFile file,
			@RequestParam(Entities.METHOD + ".technique_uncertainty") Double uncertainty,
			@RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(method);

		// TODO extract duplicate code, sanitize original file name, see
		// AnalysisDataFormats#DATA_FILE_COMPONENTS_SEPARATOR
		Path filePath = Paths.get(this.serviceManager.getBean(FileService.class)
				.buildFileNamePrefix(this.getCurrentUser(), method) + file.getOriginalFilename());

		String redirect = this.buildRedirect(method);

		if (!this.methodService.writeAnalysisFile(filePath, file)) {
			errors.add("method.update.error.analysis_file");

			return redirect;
		}

		// TODO move to service
		try {
			this.methodService.addMethodData(method, filePath.toString(), uncertainty,
					AnalysisDataFormats.RIKILT_SPCTv2);
		} catch (Exception e) {

			this.log.error("Exception during analysis file processing:", e);

			try {
				Files.deleteIfExists(filePath);
				//Files.deleteIfExists(Paths.get(filePath.toString() + ".distribution.png"));
				//Files.deleteIfExists(Paths.get(filePath.toString() + ".density.png"));
			} catch (IOException e1) {
				this.log.error("Unable to delete analysis file '{}' after failed processing.",
						filePath);
				this.log.error("Exception:", e);
			}

			errors.add("method.update.error.analysis_file");
		}

		return redirect;
	}

	/**
	 * Analysis data upload for SP-ICP-MS, sheet selection.
	 *
	 * <p>If the uploaded file has more than one non-calibration sheet, the user has to choose which
	 * one should be taken.</p>
	 */
	@PostMapping(value = "/SPICPMS/sheet")
	@RequiresUser
	public String postSpicpmsSheet(@ModelAttribute Method method, @RequestParam String sheetId,
			@RequestAttribute("errors") List<String> errors) {

		this.validateUserAwareEntities(method);

		Path filePath = this.methodService.getMethodDataFilePath(method);

		// TODO extract into method, duplicate code
		try {
			this.methodService.updateMethodData(method, sheetId);
		} catch (Exception e) {

			this.log.error("Exception during analysis file processing:", e);

			try {
				Files.deleteIfExists(Paths.get(filePath.toString() + ".distribution.png"));
				Files.deleteIfExists(Paths.get(filePath.toString() + ".density.png"));
			} catch (IOException e1) {
				this.log.error("Unable to delete result files after failed processing:",
						e1);
			}

			errors.add("method.update.error.analysis_file");
		}

		return this.buildRedirect(method);
	}
}
