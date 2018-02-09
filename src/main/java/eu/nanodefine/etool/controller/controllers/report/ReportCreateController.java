/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.report;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;

/**
 * Controller for report creation.
 */
@RequestMapping("/" + Entities.REPORT + "/" + Actions.CREATE + "/"
		+ Entities.DOSSIER + ".id={dossierId}")
@Controller
public class ReportCreateController extends AbstractReportController {

	@Value("${server.data_directory}")
	private String analysisFileDirectory;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PerformanceAttributes performanceAttributes;

	/**
	 * Show the report creation form.
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Dossier dossier, Model model) {

		this.validateUserAwareEntities(dossier);

		// Add available methods and default report name
		model.addAttribute("methods", this.serviceManager.getBean(MethodService.class)
				.loadNotArchivedDossierMethodsWithDataFile(dossier));
		model.addAttribute("reportName", this.translationService.translate("report")
				+ " " + (this.reportRepository.countByDossierUser(this.getCurrentUser()) + 1));

		return Templates.REPORT_CREATE;
	}

	/**
	 * Persists and generated the report.
	 *
	 * TODO validation
	 */
	@PostMapping
	@RequiresUser
	public String post(@RequestParam(required = false) String[] methodIds,
			@RequestParam String reportName, @ModelAttribute Dossier dossier,
			Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestAttribute("errors") List<String> errors) throws Exception {

		this.validateUserAwareEntities(dossier);

		if (methodIds == null) {
			errors.add("report.create.form.error.methods");

			return this.get(dossier, model);
		}

		this.collectReportData(dossier, Arrays.stream(methodIds)
				.mapToInt(Integer::valueOf).boxed().toArray(Integer[]::new), model);

		Report report = new Report(dossier, false, reportName, null);

		// Generate and store report
		// TODO Make choice about which type of report to use
		this.serviceManager.getBean(ReportService.class).createAndSavePdfReport(report, methodIds);

		// Archive currently active report
		this.reportService.archiveDossierReports(dossier);

		Report reportPersisted = this.reportRepository.save(report);

		return this.uriService.builder(Entities.REPORT, Actions.READ).addEntityId(reportPersisted)
				.buildRedirect();
	}
}
