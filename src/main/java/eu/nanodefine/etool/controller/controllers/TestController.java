/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;

import eu.nanodefine.etool.analysis.processors.SPCTv2Processor;
import eu.nanodefine.etool.constants.Templates;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.repositories.MethodRepository;
import eu.nanodefine.etool.model.repositories.ReportRepository;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.MailService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.report.ReportService;
import eu.nanodefine.etool.model.services.view.PlotService;

/**
 * TODO documentation.
 *
 * TODO remove
 */
@Controller
public class TestController extends AbstractController {

	@Value("${server.data_directory}")
	protected String analysisFileDirectory;

	private Logger log = LoggerFactory.getLogger(TestController.class);

	@Value("${mail.from}")
	private String mailFrom;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MailService mailService;

	@Autowired
	private MethodRepository methodRepository;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private ResourceLoader rl;

	@GetMapping(path = "/test/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public FileSystemResource download(HttpServletResponse response)
			throws Exception {

		Report report = this.reportRepository.findOne(2);

		// Extract IDs and convert to String
		String[] methodIds = Arrays.stream(this.serviceManager.getBean(
				ReportService.class).extractMethodIdsFromReport(report))
				.map(String::valueOf).toArray(String[]::new);

		this.serviceManager.getBean(ReportService.class)
				.createAndSavePdfReport(report, methodIds);

		Path reportPath = this.serviceManager.getBean(FileService.class)
				.getFullPathInAnalysisDirectory(report.getReportFileWithAttachments());
		this.listFonts(reportPath.toString());
		// TODO we need to ensure that the report name does not contain quotes
		// or escape them
		response.setHeader("Content-Disposition", "inline;filename=\""
				+ report.getName() + ".pdf\"");
		return new FileSystemResource(reportPath.toFile());
	}

	/**
	 * Finds out if the font is an embedded subset font
	 *
	 * @param name font name
	 * @return true if the name denotes an embedded subset font
	 */
	private boolean isEmbeddedSubset(String name) {
		//name = String.format("%s subset (%s)", name.substring(8), name.substring(1, 7));
		return name != null && name.length() > 8 && name.charAt(7) == '+';
	}

	/**
	 * Creates a set containing information about the not-embedded fonts within the src PDF file.
	 *
	 * @param src the path to a PDF file
	 * @throws IOException
	 */
	private void listFonts(String src) throws IOException {
		Set<String> set = new TreeSet<>();
		PdfReader reader = new PdfReader(src);
		PdfDictionary resources;
		for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
			resources = reader.getPageN(k).getAsDict(PdfName.RESOURCES);
			processResource(set, resources);
		}
		reader.close();

		for (String font : set) {
			this.log.info("Non-embedded font: {}", font);
		}
	}

	@GetMapping(value = "test/plot", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@RequiresUser
	@ResponseBody
	public FileSystemResource plot(HttpServletResponse response) throws IOException {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		Method method = this.methodRepository.findOne(2);

		Path analysisPath = this.serviceManager.getBean(FileService.class)
				.getFullPathInAnalysisDirectory(method.getDataFile());

		SPCTv2Processor processor = new SPCTv2Processor(method, null); // TODO
		processor.process(analysisPath.toString());

		Files.write(Paths.get("/tmp/dwn/plot.png"),
				this.serviceManager.getBean(PlotService.class).createSPICPMSDensityPlot(processor),
				StandardOpenOption.WRITE);

		response.setHeader("Content-Disposition", "inline; filename=\"plot.png\"");
		return new FileSystemResource("/tmp/dwn/plot.png");
	}

	private void processFont(PdfDictionary font, Set<String> set) {
		String name = font.getAsName(PdfName.BASEFONT).toString();
		if (isEmbeddedSubset(name)) {
			return;
		}

		PdfDictionary desc = font.getAsDict(PdfName.FONTDESCRIPTOR);

		//nofontdescriptor
		if (desc == null) {
			PdfArray descendant = font.getAsArray(PdfName.DESCENDANTFONTS);

			if (descendant == null) {
				set.add(name.substring(1));
			} else {
				for (int i = 0; i < descendant.size(); i++) {
					PdfDictionary dic = descendant.getAsDict(i);
					processFont(dic, set);
				}
			}
		}
		/**
		 * (Type 1) embedded
		 */
		else if (desc.get(PdfName.FONTFILE) != null) {
		} /**
		 * (TrueType) embedded
		 */
		else if (desc.get(PdfName.FONTFILE2) != null) {
		} /**
		 * " (" + font.getAsName(PdfName.SUBTYPE).toString().substring(1) + ") embedded"
		 */
		else if (desc.get(PdfName.FONTFILE3) != null) {
		} else {
			set.add(name.substring(1));
		}
	}

	/**
	 * Extracts the names of the not-embedded fonts from page or XObject resources.
	 *
	 * @param set the set with the font names
	 * @param resource the resources dictionary
	 */
	public void processResource(Set<String> set, PdfDictionary resource) {
		if (resource == null) {
			return;
		}
		PdfDictionary xobjects = resource.getAsDict(PdfName.XOBJECT);
		if (xobjects != null) {
			for (Object key : xobjects.getKeys()) {
				processResource(set, xobjects.getAsDict((PdfName) key));
			}
		}
		PdfDictionary fonts = resource.getAsDict(PdfName.FONT);
		if (fonts == null) {
			return;
		}
		PdfDictionary font;
		for (Object key : fonts.getKeys()) {
			font = fonts.getAsDict((PdfName) key);
			processFont(font, set);
		}
	}

	@GetMapping(value = "/test/error")
	public String userErrorTest(Model model) {
		model.addAttribute("error", "upload.size");

		return Templates.ERROR_USER;
	}
}
