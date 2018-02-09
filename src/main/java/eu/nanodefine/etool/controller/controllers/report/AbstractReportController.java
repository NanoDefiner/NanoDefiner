/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.report;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.repositories.ReportRepository;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;

/**
 * Abstract report controller.
 */
public class AbstractReportController extends AbstractController {

	@Autowired
	protected ReportRepository reportRepository;

	@Autowired
	protected ReportService reportService;

	/**
	 * Collect report data.
	 *
	 * <p>Extracts report methods and their results and adds them to the model.</p>
	 *
	 * TODO remove results, move to service and return only methods
	 */
	protected void collectReportData(Dossier dossier, Integer[] methodIds, Model model) {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		// TODO move to service
		Iterable<Method> methods = this.repositoryManager.getBean(MethodRepository.class)
				.findByDossierAndIdIn(dossier, methodIds);

		//List<IAnalysisProcessor> results = new ArrayList<>(methodIds.length);

		//methods.forEach(m -> results.add(ms.processAnalysisData(m)));

		model.addAttribute("methods", methods);
		//model.addAttribute("results", results);
	}

	/**
	 * Exposes report for binding.
	 *
	 * Exposes {@link eu.nanodefine.etool.controller.advice.RequestIdsAdvice#report(Optional,
	 * Optional)} to overwrite {@code binding = false}.
	 */
	@ModelAttribute("reportForm")
	public Report reportForm(@ModelAttribute Report report) {
		return report;
	}
}
