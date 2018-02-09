/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.report;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Report;

/**
 * Controller for listing user reports.
 */
@Controller
@RequestMapping("/" + Entities.REPORT + "/" + Actions.LIST)
public class ReportListController extends AbstractReportController {

	/**
	 * Shows a list of user reports.
	 *
	 * <p>Reports are separated into active, archived and all.</p>
	 */
	@GetMapping
	@RequiresUser
	public String get(Model model) {
		// TODO pagination?
		List<Report> reportsAll = this.reportService
				.loadUserReports(this.getCurrentUser()),
				reportsActive = new ArrayList<>(),
				reportsArchived = new ArrayList<>();

		// Separate reports
		for (Report r : reportsAll) {
			if (r.isArchived()) {
				reportsArchived.add(r);
			} else {
				reportsActive.add(r);
			}
		}

		// Add data to model
		model.addAttribute("reportsAll", reportsAll)
				.addAttribute("reportsActive", reportsActive)
				.addAttribute("reportsArchived", reportsArchived);

		return Templates.REPORT_LIST;
	}
}
