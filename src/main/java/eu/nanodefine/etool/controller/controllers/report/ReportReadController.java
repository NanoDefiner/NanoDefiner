/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.report;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.view.TemplateService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.utilities.classes.UriBuilder;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller for report overview.
 */
@Controller
@RequestMapping("/" + Entities.REPORT + "/" + Actions.READ + "/"
		+ Entities.REPORT + ".id={reportId}")
public class ReportReadController extends AbstractReportController {

	/**
	 * Download PDF report, optionally without attachments.
	 */
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@RequiresUser
	@ResponseBody
	public FileSystemResource download(@ModelAttribute Report report,
			@RequestParam Optional<String> noAttachments,
			HttpServletResponse response) {

		this.validateUserAwareEntities(report);

		FileService fs = this.serviceManager.getBean(FileService.class);

		// Determine file name, depending on whether attachments are to be included or not
		String fileName = report.getName() + "." + report.getId() + ".pdf";
		String reportLocation = noAttachments.isPresent() ?
				report.getReportFile() : report.getReportFileWithAttachments();

		// TODO we need to ensure that the report name does not contain quotes or escape them
		response.setHeader("Content-Disposition", "inline; filename=\""
				+ fileName + "\"");
		return new FileSystemResource(fs.getFullPathInAnalysisDirectory(reportLocation).toFile());
	}

	/**
	 * Show report overview.
	 */
	@GetMapping
	@RequiresUser
	public String get(@ModelAttribute Dossier dossier, @ModelAttribute Report report,
			Model model) {

		this.validateUserAwareEntities(dossier, report);

		Integer[] methodIds = this.reportService.extractMethodIdsFromReport(report);

		this.collectReportData(dossier, methodIds, model);
		model.addAttribute("materials", this.serviceManager.getBean(MaterialTransactionalService.class)
				.loadNotArchivedDossierMaterials(report.getDossier()));

		return Templates.REPORT_READ;
	}

	/**
	 * Add report actions to the model.
	 *
	 * <p>Report actions include PDF download and report archiving.</p>
	 */
	@ModelAttribute
	public void reportActions(@ModelAttribute Report report,
			@ModelAttribute("actionList") List<ActionListEntry> actionList) {

		if (report.getId() == 0) {
			return;
		}

		String archive = this.uriService.builder(Entities.REPORT, Actions.ARCHIVE)
				.addEntityId(report).build();

		UriBuilder downloadBuilder = this.uriService.builder(Entities.REPORT, Actions.READ)
				.addEntityId(report).addPathParam("download");

		String download = downloadBuilder.build();

		String downloadNoAttachments = downloadBuilder.addQueryParam("noAttachments").build();

		if (!report.isArchived()) {
			actionList.add(new ActionListEntry(archive, "report.read.archive",
					true));
		}

		// TODO move to service
		TranslationService translationService = this.serviceManager.getBean(TranslationService.class);
		TemplateService templateService = this.serviceManager.getBean(TemplateService.class);

		String fileSizeWithAttachments =
				templateService.generateFileSize(report.getReportFileWithAttachments());
		String fileSizeWithoutAttachments =
				templateService.generateFileSize(report.getReportFile());

		actionList.add(new ActionListEntry(download, "report.read.download.file", null, true, null,
				new Object[] { fileSizeWithAttachments }));
		actionList.add(new ActionListEntry(downloadNoAttachments,
				"report.read.download.file.no_attachments", null, true, null,
				new Object[] { fileSizeWithoutAttachments }));

	}
}
