/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.report;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.helper.history.History;

/**
 * Controller for report archival
 */
@Controller
@RequestMapping("/" + Entities.REPORT + "/" + Actions.ARCHIVE)
public class ReportArchiveController extends AbstractReportController {

	/**
	 * Archives several reports.
	 *
	 * <p>Works with an array of request parameters to determine the report IDs.</p>
	 */
	@GetMapping
	@RequiresUser
	public String archiveMany(@RequestParam Integer[] reportIds, @RequestAttribute History history) {

		this.reportService.archiveUserMethodsByIds(this.getCurrentUser(), reportIds);

		return history.getRedirect();
	}

	/**
	 * Archives a single report.
	 *
	 * <p>Works with a path variable and transforms it into a call to
	 * {@link #archiveMany(Integer[], History)}.</p>
	 */
	@GetMapping(value = "/" + Entities.REPORT + ".id={reportId}")
	@RequiresUser
	public String archiveOne(@PathVariable Integer reportId, @RequestAttribute History history) {
		return this.archiveMany(new Integer[] { reportId }, history);
	}

}
