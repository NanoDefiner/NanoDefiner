/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.report;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.lowagie.text.pdf.PdfFileSpecification;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.controllers.material.MaterialFeedbackController;
import eu.nanodefine.etool.knowledge.configurations.MaterialConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredMaterial;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.ReferenceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.MethodAnalysisServiceManager;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.helpers.technique.HashResult;
import eu.nanodefine.etool.model.helpers.technique.TechniqueNameComparator;
import eu.nanodefine.etool.model.helpers.technique.TechniqueTierComparator;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.material.MaterialCriterionService;
import eu.nanodefine.etool.model.services.material.MaterialService;
import eu.nanodefine.etool.model.services.material.MaterialTransactionalService;
import eu.nanodefine.etool.model.services.method.constants.MethodAttributes;
import eu.nanodefine.etool.model.services.technique.TechniqueService;
import eu.nanodefine.etool.model.services.user.UserService;
import eu.nanodefine.etool.model.services.view.NumberService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;
import eu.nanodefine.etool.view.report.DynamicPdfReport;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.FieldBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.PdfaConformance;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * Service for creating a dynamic PDF report.
 */
@Service
public class DynamicReportService
		implements IPdfReportService<JasperReportBuilder> {

	/**
	 * Report locale.
	 *
	 * TODO move to configuration
	 */
	private static Locale REPORT_LOCALE = Locale.UK;

	private final MethodAnalysisServiceManager analysisServiceManager;

	private final String brandLocation;

	private final DynamicReportComponentService cs;

	private final String institution;

	private final MaterialConfiguration materialConfiguration;

	private final NumberService ns;

	private final PerformanceConfiguration performanceConfiguration;

	private final PerformanceDictionary performanceDictionary;

	private final ReferenceDictionary referenceDictionary;

	private final ResourceLoader resourceLoader;

	private final ServiceManager serviceManager;

	private final Table<String, String, HashResult> techniqueHashes;

	private final TechniqueTierComparator techniqueTierComparator;

	private final List<Technique> techniques;

	private final TranslationService ts;

	private Logger log = LoggerFactory.getLogger(DynamicReportService.class);

	/**
	 * List of techniques filtered for the report dossier.
	 *
	 * @see #provideModelAttributes(DynamicPdfReport)
	 * @see TechniqueService#filterTechniquesForDossier(List, Dossier)
	 */
	private List<Technique> techniquesFiltered;

	@Autowired
	public DynamicReportService(MethodAnalysisServiceManager analysisServiceManager,
			@Value("${custom.brand}") String brandLocation,
			DynamicReportComponentService dynamicReportComponentService,
			@Value("${custom.institution}") String institution,
			MaterialConfiguration materialConfiguration, NumberService numberService,
			PerformanceConfiguration performanceConfiguration,
			PerformanceDictionary performanceDictionary, ReferenceDictionary referenceDictionary,
			ResourceLoader resourceLoader,
			ServiceManager serviceManager,
			@Qualifier("techniqueHashes") Table<String, String, HashResult> techniqueHashes,
			TechniqueTierComparator techniqueTierComparator,
			@Qualifier("techniques") List<Technique> techniques,
			TranslationService translationService) {

		this.analysisServiceManager = analysisServiceManager;
		this.brandLocation = brandLocation;
		this.cs = dynamicReportComponentService;
		this.institution = institution;
		this.materialConfiguration = materialConfiguration;
		this.ns = numberService;
		this.performanceConfiguration = performanceConfiguration;
		this.performanceDictionary = performanceDictionary;
		this.referenceDictionary = referenceDictionary;
		this.resourceLoader = resourceLoader;
		this.serviceManager = serviceManager;
		this.techniqueHashes = techniqueHashes;
		this.techniqueTierComparator = techniqueTierComparator;
		this.techniques = techniques;
		this.ts = translationService;
	}

	/**
	 * Add acknowledgement and funding information to the given report.
	 */
	private List<DynamicPdfReport> acknowledgementInfo(DynamicPdfReport summaryReport) {
		DynamicPdfReport pdfReport = new DynamicPdfReport(summaryReport);

		pdfReport.addTitle(
				this.cs.createHeadlineMain(this.translate("report.pdf.acknowledgement.heading")),
				this.cs.createHeadlineFiller());

		pdfReport.addSummary(this.cs.createText(this.translate("global.funding")));
		pdfReport.setPageBreakBefore(false);

		return ImmutableList.of(pdfReport);
	}

	/**
	 * Add attachments to the PDF file associated with the given report.
	 *
	 * <p>Attachments can come from method results, as well as from dossier, material or method
	 * custom attributes.</p>
	 *
	 * @see ReportService#extractEmbeddableFiles(Report)
	 */
	private void addAttachments(Report report) throws Exception {

		FileService fileService = this.serviceManager.getBean(FileService.class);
		String fullReportPath =
				fileService.getFullPathInAnalysisDirectory(report.getReportFile()).toString();

		// The report with attachments will be written to a new file
		String reportWithAttachmentsPath = fullReportPath.substring(0, fullReportPath.length() - 4)
				+ "_attachments.pdf";

		FileOutputStream outputStream = new FileOutputStream(reportWithAttachmentsPath, false);

		PdfReader reader = new PdfReader(fullReportPath);
		PdfStamper stamper = new PdfStamper(reader, outputStream);

		Map<String, File> attachments = this.serviceManager.getBean(ReportService.class)
				.extractEmbeddableFiles(report);

		PdfFileSpecification fs;

		// Create an attachment for each extracted file
		for (Map.Entry<String, File> entry : attachments.entrySet()) {
			fs = PdfFileSpecification.fileEmbedded(stamper.getWriter(),
					entry.getValue().getAbsolutePath(), entry.getKey(), null, true);

			stamper.addFileAttachment(entry.getKey(), fs);
		}

		stamper.close();
	}

	private void addTechniqueHashWarning(DynamicPdfReport pdfReport, Technique technique,
			List<Material> materials) {

		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);

		String techniqueSignifier = technique.getSignifier(), materialSignifier;
		HashResult hashResult;
		Map<String, String> materialCriteria;
		boolean invalid = false;

		for (Material material : materials) {
			materialCriteria = mcs.createMaterialCriteriaMap(material, false);
			materialSignifier = materialCriteria.getOrDefault(PerformanceAttributes.MATERIAL_SIGNIFIER,
					PerformanceDictionary.DEFAULT_MATERIAL_SIGNIFIER);

			hashResult = this.techniqueHashes.get(techniqueSignifier, materialSignifier);

			if (hashResult == null) {
				this.log.warn("No hash for technique signifier {} and material signifier {}",
						techniqueSignifier, materialSignifier);
			}

			invalid = hashResult != null && hashResult.isInvalid();

			if (invalid) {
				break;
			}
		}

		if (!invalid) {
			return;
		}

		this.addWarning(pdfReport, this.translate("report.pdf.technique.hash_warning"));
	}

	/**
	 * Add technique incompleteness information for a single technique to the given report.
	 */
	private void addTechniqueIncompleteness(DynamicPdfReport report, Technique technique,
			Map<String, List<Double>> techniqueIncompletenessMap) {
		// May be null if technique does not exist in KB anymore
		List<Double> incompletenesses = techniqueIncompletenessMap.get(technique.getSignifier());

		String techniqueIncompleteness = this.ns.formatPercentage(1 - (
				incompletenesses != null ? incompletenesses.get(0) : 0.));
		String weightedTechniqueIncompleteness = this.ns.formatPercentage(1 -
				(incompletenesses != null ? incompletenesses.get(1) : 0.));

		report.addRow(
				this.translate("method.table.column.incompleteness_title"), techniqueIncompleteness)
				.addRow(this.translate("method.table.column.weighted_incompleteness_title"),
						weightedTechniqueIncompleteness);
	}

	/**
	 * Add technique warnings for a single technique to the given report.
	 *
	 * TODO does this properly support multiconstituent?
	 */
	private void addTechniqueWarnings(DynamicPdfReport pdfReport,
			Technique technique,
			Map<String, String> techniqueGeneralWarnings,
			Table<String, Attribute, List<Material>> techniqueMaterialWarnings) {

		String signifier = technique.getSignifier();

		// Display warning if technique hash is not valid

		// General warnings are basically disclaimers which are always displayed for this technique
		if (techniqueGeneralWarnings.containsKey(signifier)) {
			this.addWarning(pdfReport, techniqueGeneralWarnings.get(signifier));
		}

		Map<Attribute, List<Material>> techniqueMaterialWarningsRow =
				techniqueMaterialWarnings.row(signifier);

		if (techniqueMaterialWarningsRow.isEmpty()) {
			return;
		}

		this.addWarning(pdfReport,
				this.translate("report.pdf.technique.unknown_attributes_warning"));

		DynamicPdfReport warningReport = new DynamicPdfReport(pdfReport);
		this.cs.createStringTable(warningReport, null,
				new String[] { this.translate("global.table.column.attribute"),
						this.translate("report.pdf.technique.table.column.value") });

		String attributeLabel, techniqueValueRaw, techniqueValueTranslated, techniqueValueText;

		// Add warnings for each unknown material attribute that can lead to technique exclusion
		Attribute a;
		for (Map.Entry<Attribute, List<Material>> entry : techniqueMaterialWarningsRow.entrySet()) {
			a = entry.getKey();

			attributeLabel =
					this.referenceDictionary.getAttribute(a.getName()).getLabel();

			techniqueValueRaw = this.performanceConfiguration
					.getEntry(technique.getSignifier())
					.getEntry(a.getName()).getValue();

			techniqueValueTranslated = this.performanceDictionary
					.translateValue(a.getName(), techniqueValueRaw);

			techniqueValueText =
					this.performanceDictionary.getAttribute(a.getName()).getLabel() + ":<br />"
							+ techniqueValueTranslated;

			warningReport.addRow(attributeLabel, techniqueValueText);
		}

		pdfReport.addSummary(warningReport.getSubreport());
	}

	/**
	 * Add a warning with the given text to the given report.
	 *
	 * @see #addWarning(DynamicPdfReport, String, int, int)
	 */
	private void addWarning(DynamicPdfReport pdfReport, String text) {
		this.addWarning(pdfReport, text, 0, 2);
	}

	/**
	 * Add a warning with the given text and surrounding fillers to the given report.
	 */
	private void addWarning(DynamicPdfReport pdfReport, String text, int fillersBefore,
			int fillersAfter) {
		pdfReport.addSummary(this.cs.createDefaultFiller(fillersBefore),
				this.cs.createText("<b>" + this.translate("global.warning") + ":</b> " + text, Markup.HTML),
				this.cs.createDefaultFiller(fillersAfter));
	}

	/**
	 * Add appendix informatino to the given report.
	 *
	 * <p>Currently, the appendix contains:</p>
	 *
	 * <ul>
	 *   <li>A glossary (see {@link #createGlossary(DynamicPdfReport)})</li>
	 *   <li>A technique overview table (see {@link #createTechniqueOverview(DynamicPdfReport)})</li>
	 *   <li>Unsuitability details for each unsuitable technique (see
	 *    {@link #createUnsuitabilityDetails(DynamicPdfReport)})</li>
	 * </ul>
	 */
	private List<DynamicPdfReport> appendixInfo(DynamicPdfReport summaryReport) {
		DynamicPdfReport pdfReport = new DynamicPdfReport(summaryReport);

		pdfReport.addTitle(this.cs.createHeadlineMain(this.translate("report.pdf.appendix.heading")),
				this.cs.createHeadlineFiller());

		pdfReport.addSummary(this.createGlossary(summaryReport).getSubreport(),
				DynamicReports.cmp.pageBreak());

		pdfReport.addSummary(this.createTechniqueOverview(pdfReport).getSubreport(),
				DynamicReports.cmp.pageBreak());

		pdfReport.addSummary(this.cs.createSubreportFiller(),
				this.createUnsuitabilityDetails(pdfReport).getSubreport());

		return ImmutableList.of(pdfReport);
	}

	@Override
	public JasperReportBuilder createAndWritePdfReport(Report report,
			List<Method> methods) throws Exception {

		// TODO export constants

		FileService fileService = this.serviceManager.getBean(FileService.class);

		// Set up PDF/a compliant font embedding
		JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		jasperReportsContext.setProperty("net.sf.jasperreports.default.pdf.font.name",
				"net/sf/jasperreports/fonts/dejavu/DejaVuSans.ttf");
		jasperReportsContext.setProperty("net.sf.jasperreports.default.pdf.embedded", "true");

		DynamicPdfReport reportSummary = new DynamicPdfReport();
		reportSummary.addModelAttribute("report", report);
		reportSummary.addModelAttribute("methods", methods);

		// Add common model attributes
		this.provideModelAttributes(reportSummary);

		// Set up PDF report settings
		reportSummary.getJasperReport()
				.setPageMargin(DynamicReports.margin(40))
				.pageFooter(DynamicReports.cmp.pageXofY());

		// TODO extract image stuff into method

		// TODO make this configurable
		BufferedImage companyBrandImage = this.cs.removeImageTransparency(
				ImageIO.read(this.resourceLoader.getResource(this.brandLocation).getInputStream()));

		ImageBuilder companyBrand = DynamicReports.cmp.image(companyBrandImage)
				.setFixedDimension(Math.round(companyBrandImage.getWidth() * .3f),
						Math.round(companyBrandImage.getHeight() * .3f))
				.setHorizontalImageAlignment(HorizontalImageAlignment.LEFT);

		BufferedImage nanodefineBrandImage = this.cs.removeImageTransparency(
				ImageIO.read(this.resourceLoader.getResource("/img/nd_orig.png").getInputStream()));

		ImageBuilder nanodefineBrand = DynamicReports.cmp.image(nanodefineBrandImage)
				.setDimension(Math.round(nanodefineBrandImage.getWidth() * .3f),
						Math.round(nanodefineBrandImage.getHeight() * .3f))
				.setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT);

		reportSummary.getJasperReport().title(DynamicReports.cmp.horizontalList()
				.add(companyBrand, nanodefineBrand).newRow(25));

		// TODO avoid method chain
		reportSummary.addTitle(this.cs.createHeadlineMain(this.translate("report.pdf.report_heading",
				reportSummary.getReport().getDossier().getName())));

		TextFieldBuilder<String> description = DynamicReports.cmp.text(
				this.translate("report.pdf.report_description"));

		reportSummary.getJasperReport().title(DynamicReports.cmp.horizontalList()
				.newRow(25).add(description));

		reportSummary.addSummary(this.cs.createSubreportFiller());

		// Collect all sub-reports
		List<DynamicPdfReport> subreports = new ArrayList<>(
				this.dossierInfo(reportSummary));
		subreports.addAll(this.sampleInfo(reportSummary));
		subreports.addAll(this.materialInfo(reportSummary));
		subreports.addAll(this.methodInfo(reportSummary));
		subreports.addAll(this.acknowledgementInfo(reportSummary));
		subreports.addAll(this.appendixInfo(reportSummary));

		// Add all sub-reports
		for (DynamicPdfReport subreport : subreports) {
			if (subreport.isPageBreakBefore()) {
				reportSummary.addSummary(DynamicReports.cmp.pageBreak());
			} else {
				reportSummary.addSummary(this.cs.createSubreportFiller());
			}

			reportSummary.addSummary(subreport.getSubreport());

		}

		// TODO extract file writing to method
		FileOutputStream outputStream = new FileOutputStream(
				fileService.getFullPathInAnalysisDirectory(report.getReportFile()).toFile(), false);

		// TODO fix PDF/a conformance
		JasperPdfExporterBuilder exporterBuilder = Exporters.pdfExporter(outputStream);
		exporterBuilder.setPdfaConformance(PdfaConformance.PDFA_1A);
		exporterBuilder.setIccProfilePath("pdf/sRGB2014.icc");
		exporterBuilder.setTagged(true);
		exporterBuilder.setTagLanguage("en-gb");

		reportSummary.getJasperReport().summaryWithPageHeaderAndFooter().export(exporterBuilder);

		outputStream.close();

		// Add attachments after initial file was written
		this.addAttachments(report);

		return reportSummary.getJasperReport().summaryWithPageHeaderAndFooter();
	}

	/**
	 * Creates a custom attribute table for the given entity.
	 */
	private DynamicPdfReport createCustomAttributeTable(DynamicPdfReport parentReport,
			ICustomAttributeEntity entity) {
		DynamicPdfReport pdfReport = new DynamicPdfReport(parentReport);

		pdfReport.addTitle(this.cs.createDefaultFiller(),
				this.cs.createHeadlineSubSub(this.translate("custom_attribute.plural")),
				this.cs.createDefaultFiller());
		this.cs.createStringTable(pdfReport, new String[] { "name", "value", "comment" },
				new String[] { this.translate("global.table.column.name"),
						this.translate("global.table.column.value"),
						this.translate("global.table.column.comment") });

		FileService fs = this.serviceManager.getBean(FileService.class);
		String value;
		// TODO sort
		for (CustomAttribute customAttribute : entity.getCustomAttributes()) {
			value = customAttribute.isFile() ? this.translate("report.pdf.custom_attribute.attachment",
					fs.extractOriginalFileName(customAttribute.getValue())) : customAttribute.getValue();

			pdfReport.addRow(customAttribute.getName(), value,
					this.cs.createString(customAttribute.getComment()));
		}

		return pdfReport;
	}

	/**
	 * Creates the glossary.
	 */
	private DynamicPdfReport createGlossary(DynamicPdfReport summaryReport) {
		DynamicPdfReport pdfReport = new DynamicPdfReport(summaryReport);

		pdfReport.addTitle(this.cs.createHeadlineSub(this.translate("report.pdf.glossary.heading")),
				this.cs.createHeadlineFiller());

		pdfReport.addSummary(this.cs
						.createText("<b>" + this.translate("material.create.incompleteness.label") + "</b>"),
				this.cs.createDefaultFiller());

		pdfReport.addSummary(DynamicReports.cmp
				.horizontalList(DynamicReports.cmp.horizontalGap(10),
						this.cs.createText(
								this.translate("material.create.incompleteness.popover.explanation") +
										"<br /><br />" + "<em>C<sup>m</sup></em> &lt; 50% = "
										+ this.translate("material.create.incompleteness.popover.low")
										+ "<br />" + "<em>C<sup>m</sup></em> ≥ 50% = "
										+ this.translate("material.create.incompleteness.popover.moderate")
										+ "<br />" + "<em>C<sup>m</sup></em> &gt; 80% = "
										+ this.translate("material.create.incompleteness.popover.high"),
								Markup.HTML)), this.cs.createDefaultFiller());

		pdfReport.addSummary(this.cs.createText(
				"<b>" + this.translate("material.table.modal.technique.incompleteness.label") + "</b>"),
				this.cs.createDefaultFiller());

		pdfReport.addSummary(DynamicReports.cmp
				.horizontalList(DynamicReports.cmp.horizontalGap(10),
						this.cs.createText(this.translate("material.table.modal.technique.popover.content")
										+ "<br /><br /><em>Ĉ<sup>m</sup></em> &lt; 50% = "
										+ this.translate("material.table.modal.technique.popover.low")
										+ "<br /><em>Ĉ<sup>m</sup></em> ≥ 50% = "
										+ this.translate("material.table.modal.technique.popover.moderate")
										+ "<br /><em>Ĉ<sup>m</sup></em> &gt; 80% = "
										+ this.translate("material.table.modal.technique.popover.high"),
								Markup.HTML)), this.cs.createDefaultFiller());

		pdfReport.addSummary(this.cs.createText(
				"<b>" + this.translate("method.table.column.incompleteness_title") + "</b>"),
				this.cs.createDefaultFiller());

		pdfReport.addSummary(DynamicReports.cmp
				.horizontalList(DynamicReports.cmp.horizontalGap(10),
						this.cs.createText(
								this.translate("report.pdf.glossary.weighted_technique_incompleteness"),
								Markup.HTML)));

		return pdfReport;
	}

	/**
	 * Creates material incompleteness information for the given material and technique.
	 */
	private DynamicPdfReport createMaterialIncompletenessDetails(DynamicPdfReport pdfReport,
			Material material, Technique technique) {

		// Extract model attributes
		Table<String, String, MaterialCriterionTechniqueCompatibility>
				compatibilityTable = ((Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>) pdfReport
				.getModelAttribute("materialCriteriaTechniqueCompatibilityTableMap"))
				.get(material);

		Table<Material, String, Double> materialIncompletenessTable =
				(Table<Material, String, Double>) pdfReport
						.getModelAttribute("materialIncompletenessTable");

		String weightedMaterialIncompleteness = this.ns.formatPercentage(
				1 - materialIncompletenessTable.get(material, technique.getSignifier()));

		DynamicPdfReport overviewReport = new DynamicPdfReport(pdfReport);

		overviewReport.addTitle(this.cs.createDefaultFiller(),
				this.cs.createText(material.getName()).setStyle(
						DynamicReports.stl.style(this.cs.getTextStyle()).italic().bold()));
		this.cs.createStringTable(overviewReport);
		overviewReport.getJasperReport().setHighlightDetailEvenRows(false);

		overviewReport.addRow(this.translate("material.table.modal.technique.incompleteness.label"),
				weightedMaterialIncompleteness);

		// Create table
		DynamicPdfReport detailReport = new DynamicPdfReport(overviewReport);
		this.cs.createStringTable(detailReport,
				new String[] { "attribute", "technique", "material", "explanation" },
				new String[] { this.translate("global.table.column.attribute"),
						this.translate("technique"), this.translate("material") });
		// No highlighting since we have explanations in a separate row and highlighting doesn't work
		// properly in that case
		detailReport.getJasperReport().setHighlightDetailEvenRows(false);

		// Collect mismatch information
		String label, techniqueValue, materialValue;
		boolean suitable = true;
		for (MaterialCriterion mc : material.getMaterialCriterions()) {
			// Only process mismatches which have explanations
			if (!compatibilityTable.get(mc.getName(), technique.getSignifier()).getMatchReason()
					.equals(ExplanationDictionary.MISMATCH) || compatibilityTable.get(mc.getName(),
					technique.getSignifier()).getExplanation().equals("")) {
				continue;
			}

			suitable = false;

			label = this.referenceDictionary.getAttribute(mc.getName()).getLabel();
			techniqueValue =
					this.performanceDictionary.getAttribute(mc.getName()).getLabel() + ":<br />"
							+ this.performanceDictionary.translateValue(mc.getName(),
							this.performanceConfiguration.getEntry(technique.getSignifier())
									.getEntry(mc.getName()).getValue());

			materialValue = label + ":<br />" + this.performanceDictionary
					.translateValue(mc.getName(), mc.getValue());

			detailReport.addRow(label, techniqueValue, materialValue,
					compatibilityTable.get(mc.getName(), technique.getSignifier()).getExplanation());
		}

		overviewReport.addSummary(this.cs.createDefaultFiller());

		if (suitable) {
			overviewReport.addSummary(this.cs.createText(
					this.translate("report.pdf.technique.material.incompleteness.suitable")));
		} else {
			FieldBuilder<String> explanationField = DynamicReports
					.field("explanation", DataTypes.stringType());
			detailReport.getJasperReport().addField(explanationField);

			detailReport.getJasperReport()
					.detailFooter(
							DynamicReports.cmp.text(
									this.translate("material.table.modal.technique.column.explanation") + ":"),
							DynamicReports.cmp.text(explanationField));
			detailReport.getJasperReport().detailHeader(DynamicReports.cmp.line());

			overviewReport.addSummary(detailReport.getSubreport());
		}

		overviewReport.addSummary(this.cs.createDefaultFiller());

		return overviewReport;
	}

	/**
	 * Creates technique overview table.
	 */
	private DynamicPdfReport createTechniqueOverview(DynamicPdfReport pdfReport) {
		DynamicPdfReport techniqueOverview = new DynamicPdfReport(pdfReport);

		techniqueOverview.addTitle(
				this.cs.createHeadlineSub(this.translate("report.pdf.technique.overview_heading")),
				this.cs.createHeadlineFiller());
		this.cs.createStringTable(techniqueOverview,
				new String[] { "technique", "tier", "available", "suitable", "ti", "wti" },
				new String[] { this.translate("technique"),
						this.translate("method.table.column.tier"),
						this.translate("method.table.column.available"),
						this.translate("method.table.column.suitable"),
						this.translate("method.table.column.incompleteness.report"),
						this.translate("method.table.column.weighted_incompleteness.report") });

		techniqueOverview.getColumn("technique").setWidth(60);
		techniqueOverview.getColumn("available").setWidth(40);
		techniqueOverview.getColumn("suitable").setWidth(40);
		techniqueOverview.getColumn("ti").setWidth(30);
		techniqueOverview.getColumn("wti").setWidth(30);

		List<Technique> unavailableTechniques = (List<Technique>) techniqueOverview
				.getModelAttribute("unavailableTechniques");
		Map<String, String> techniqueSuitabilityMap = (Map<String, String>) techniqueOverview
				.getModelAttribute("techniqueSuitabilityMap");
		Map<String, List<Double>> techniqueIncompletenessMap =
				(Map<String, List<Double>>) techniqueOverview
						.getModelAttribute("techniqueIncompletenessMap");

		String tierRaw, tierTranslated;
		boolean available, suitable, anyTier2Available = false;
		for (Technique t : this.techniquesFiltered) {
			available = !unavailableTechniques.contains(t);
			suitable = techniqueSuitabilityMap.get(t.getSignifier())
					.equals(MaterialFeedbackController.SUITABILITY_YES);
			tierRaw = this.performanceConfiguration.getEntry(t.getSignifier())
					.getEntry(PerformanceAttributes.TIER).getValue();
			tierTranslated = this.performanceDictionary
					.translateValue(PerformanceAttributes.TIER, tierRaw);
			String techniqueIncompleteness = this.ns.formatPercentage(
					1 - techniqueIncompletenessMap.get(t.getSignifier()).get(0));
			String weightedTechniqueIncompleteness = this.ns.formatPercentage(
					1 - techniqueIncompletenessMap.get(t.getSignifier()).get(1));
			techniqueOverview.addRow(t.getName(), tierTranslated,
					available ? this.translate("global.true") : this.translate("global.false"),
					suitable ? this.translate("global.true") : this.translate("global.false"),
					techniqueIncompleteness, weightedTechniqueIncompleteness);

			if (tierRaw.contains("tier2") && available && suitable) {
				anyTier2Available = true;
			}
		}

		// TODO only display if really no tier 2 methods were used
		if (!anyTier2Available) {
			techniqueOverview.addSummary(this.cs.createDefaultFiller(), this.cs.createText(
					this.translate("report.pdf.appendix.technique.overview.no_tier2")),
					this.cs.createDefaultFiller());
		}

		return techniqueOverview;
	}

	/**
	 * Creates unsuitability details for unsuitable techniques.
	 */
	private DynamicPdfReport createUnsuitabilityDetails(
			DynamicPdfReport pdfReport) {
		List<Technique> unavailableTechniques = (List<Technique>) pdfReport
				.getModelAttribute("unavailableTechniques");
		Map<String, String> techniqueSuitabilityMap = (Map<String, String>) pdfReport
				.getModelAttribute("techniqueSuitabilityMap");
		Map<String, List<Double>> techniqueIncompletenessMap =
				(Map<String, List<Double>>) pdfReport
						.getModelAttribute("techniqueIncompletenessMap");
		List<Material> materials = (List<Material>) pdfReport.getModelAttribute("materials");

		DynamicPdfReport unsuitabilityDetails = new DynamicPdfReport(pdfReport);
		unsuitabilityDetails.addTitle(
				this.cs.createHeadlineSub(this.translate("report.pdf.appendix.unsuitability.heading")),
				this.cs.createHeadlineFiller());
		DynamicPdfReport unsuitabilityDetailsSummary,
				unsuitabilityDetailsTechniqueInfo, unsuitabilityDetailsReasons;

		String tierRaw, tierTranslated;
		boolean available, suitable;
		for (Technique t : this.techniquesFiltered) {
			available = !unavailableTechniques.contains(t);
			suitable = techniqueSuitabilityMap.get(t.getSignifier())
					.equals(MaterialFeedbackController.SUITABILITY_YES);
			tierRaw = this.performanceConfiguration.getEntry(t.getSignifier())
					.getEntry(PerformanceAttributes.TIER).getValue();
			tierTranslated = this.performanceDictionary
					.translateValue(PerformanceAttributes.TIER, tierRaw);

			if (!available || suitable) {
				continue;
			}

			unsuitabilityDetailsSummary = new DynamicPdfReport(pdfReport);

			unsuitabilityDetailsTechniqueInfo =
					new DynamicPdfReport(unsuitabilityDetailsSummary);

			unsuitabilityDetailsTechniqueInfo
					.addTitle(this.cs.createHeadlineSubSub(t.getName()));

			this.cs.createStringTable(unsuitabilityDetailsTechniqueInfo);
			unsuitabilityDetailsTechniqueInfo.addRow(this.translate("global.table.column.name"),
					t.getComment()).addRow(this.translate("method.table.column.tier"), tierTranslated);

			this.addTechniqueIncompleteness(unsuitabilityDetailsTechniqueInfo, t,
					techniqueIncompletenessMap);

			// Prevent page-break
			unsuitabilityDetailsSummary.addSummary(
					unsuitabilityDetailsTechniqueInfo.getSubreportWithoutPageBreaks(),
					this.cs.createDefaultFiller());

			unsuitabilityDetailsReasons =
					new DynamicPdfReport(unsuitabilityDetailsSummary);
			this.cs.createStringTable(unsuitabilityDetailsReasons,
					new String[] { "attribute", "technique", "material", "explanation", "counter" },
					new String[] { this.translate("global.table.column.attribute"),
							this.translate("technique"), this.translate("material") });
			unsuitabilityDetailsReasons.getJasperReport().setHighlightDetailEvenRows(false);

			// Create material incompleteness details for this technique
			DynamicPdfReport materialReport;
			for (Material material : materials) {

				materialReport = this.createMaterialIncompletenessDetails(unsuitabilityDetailsSummary,
						material, t);

				unsuitabilityDetailsSummary.addSummary(materialReport.getSubreport());
			}

			unsuitabilityDetails.addSummary(unsuitabilityDetailsSummary.getSubreport(),
					DynamicReports.cmp.pageBreak());
		}

		return unsuitabilityDetails;
	}

	/**
	 * Create dossier information.
	 */
	private List<DynamicPdfReport> dossierInfo(DynamicPdfReport summaryReport) {
		Dossier dossier = summaryReport.getReport().getDossier();

		DynamicPdfReport pdfReport = new DynamicPdfReport(summaryReport);
		pdfReport.addTitle(this.cs.createHeadlineSub(this.translate("report.pdf.dossier.heading")),
				this.cs.createHeadlineFiller());

		this.cs.createStringTable(pdfReport);
		pdfReport.addRow(this.translate("global.table.column.name"), dossier.getName())
				.addRow(this.translate("global.table.column.comment"),
						this.cs.createString(dossier.getComment()));

		// Add custom attributes if any exist
		if (!dossier.getCustomAttributes().isEmpty()) {
			pdfReport.addSummary(this.createCustomAttributeTable(pdfReport, dossier).getSubreport());
		}

		return ImmutableList.of(pdfReport);
	}

	/**
	 * Create material information.
	 */
	private List<DynamicPdfReport> materialInfo(DynamicPdfReport summaryReport) {

		NumberService ns = this.serviceManager.getBean(NumberService.class);

		Table<Material, String, Double> materialIncompletenessTable =
				(Table<Material, String, Double>) summaryReport
						.getModelAttribute("materialIncompletenessTable");

		Collection<Material> materials = (Collection<Material>) summaryReport
				.getModelAttribute("materials");

		List<DynamicPdfReport> materialReports = new ArrayList<>();

		MaterialCriterionDDOMap materialCriterionDDOMap;
		DynamicPdfReport materialReport;
		int i = 1;
		Double incompleteness;
		for (Material m : materials) {
			materialCriterionDDOMap = new MaterialCriterionDDOMap(m.getMaterialCriterions());
			incompleteness = materialIncompletenessTable.get(m, "material");
			materialReport = new DynamicPdfReport(summaryReport);
			this.cs.createStringTable(materialReport);

			// Create main material heading before the first material
			if (i == 1) {
				materialReport.addTitle(
						this.cs.createHeadlineSub(this.translate("report.pdf.material.heading")),
						this.cs.createHeadlineFiller());
			}

			// Add comment and incompleteness
			materialReport.addTitle(this.cs
							.createHeadlineSubSub(m.getName()), this.cs.createDefaultFiller(),
					this.cs.createText(this.translate("global.table.column.comment") + ": " +
							this.cs.createString(m.getComment())),
					this.cs.createText(this.translate("material.create.incompleteness.label") + ": " +
							ns.formatPercentage(1 - incompleteness)));

			// List material criteria
			ConfiguredMaterial configuredMaterial;
			Attribute attribute;
			String label, value;
			for (MaterialCriterionDDO mc : materialCriterionDDOMap.values()) {
				attribute = this.referenceDictionary.getAttribute(mc.getName());

				// Special case for material signifier
				// TODO solve cleaner, add material signifier in reference dictionary
				if (attribute == null) {
					if (mc.getName().equals(PerformanceAttributes.MATERIAL_SIGNIFIER)) {
						configuredMaterial = this.materialConfiguration.getEntry(mc.getValue());

						label = this.translate("material.table.column.signifier");
						value = configuredMaterial != null ? configuredMaterial.getName() : mc.getValue();
					} else {
						this.log.warn("Skipped unknown material criterion: {}", mc.getName());
						continue;
					}
				} else {
					label = attribute.getLabel();
					value = this.performanceDictionary
							.translateValue(mc.getName(), mc.getValue());
				}

				materialReport.addRow(label, value);
			}

			// Add custom attributes if any exist
			if (!m.getCustomAttributes().isEmpty()) {
				materialReport.addSummary(this.createCustomAttributeTable(materialReport, m)
						.getSubreport());
			}

			materialReports.add(materialReport);
			i++;
		}

		materialReports.get(0).setPageBreakBefore(true);

		return materialReports;
	}

	/**
	 * Creates method information.
	 */
	private List<DynamicPdfReport> methodInfo(DynamicPdfReport summaryReport)
			throws IOException {

		List<DynamicPdfReport> methodReports = new ArrayList<>();

		// Collect model attributes
		List<Method> methods =
				(List<Method>) summaryReport.getModelAttribute("methods");

		List<Material> materials = (List<Material>) summaryReport
				.getModelAttribute("materials");

		Map<String, String> techniqueGeneralWarnings =
				(Map<String, String>) summaryReport
						.getModelAttribute("techniqueGeneralWarningsMap");

		Table<String, Attribute, List<Material>> techniqueMaterialWarnings =
				(Table<String, Attribute, List<Material>>) summaryReport
						.getModelAttribute("techniqueMaterialWarningsTable");

		Map<String, List<Double>> techniqueIncompletenessMap =
				(Map<String, List<Double>>) summaryReport
						.getModelAttribute("techniqueIncompletenessMap");

		Map<String, String> techniqueSuitabilityMap = (Map<String, String>) summaryReport
				.getModelAttribute("techniqueSuitabilityMap");

		DynamicPdfReport methodReport;
		String tier, preparation;
		int i = 1;
		for (Method m : methods) {
			methodReport = new DynamicPdfReport(summaryReport);
			this.cs.createStringTable(methodReport);

			// Add main heading before first method
			if (i == 1) {
				methodReport.addTitle(
						this.cs.createHeadlineSub(this.translate("report.pdf.method.heading")),
						this.cs.createHeadlineFiller());
			}

			methodReport.addTitle(this.cs.createHeadlineSubSub(m.getName()));

			tier = this.performanceDictionary.translateValue(PerformanceAttributes.TIER, m.getTier());
			preparation = this.performanceDictionary.translateValue(PerformanceAttributes.PREPARATION,
					m.getPreparation() != null ? m.getPreparation() : "{}");

			methodReport.addRow(this.translate("method.table.column.tier"), tier)
					.addRow(this.translate("global.table.column.comment"),
							this.cs.createString(m.getComment()))
					.addRow(this.translate("method.read.table.column.preparation"), preparation)
					.addRow(this.translate("method.read.table.column.preparation_comment"),
							this.cs.createString(m.getPreparationComment()));

			// Add custom attributes if any exist
			if (!m.getCustomAttributes().isEmpty()) {
				methodReport.addSummary(this.createCustomAttributeTable(methodReport, m).getSubreport());
			}

			this.addTechniqueIncompleteness(methodReport, m.getTechnique(), techniqueIncompletenessMap);

			// Add anaylsis data information
			String dataFormat = this.translate(
					"report.create.pdf.analysis_data_format." + m.getDataFormat());
			String result = m.getResult() != null ?
					this.translateDouble("number_format.analysis_result",
							Double.valueOf(m.getResult())) + (m.getDataFormat().equals("VSSA") && !m.getDossier().getPurpose().equals("{other_2011}") ? "m²/cm³" : "nm") : "–";

			methodReport.addRow(this.translate("method.table.column.data_format"), dataFormat)
					.addRow(this.translate("method.table.column.result"), result)
					// TODO extract into method
					.addRow(this.translate("method.table.column.uncertainty"),
							m.getTechniqueUncertainty() > 0 ? m.getTechniqueUncertainty() + "%"
									: this.translate("global.value.unknown"));

			this.analysisServiceManager.getBean(m.getDataFormat()).addMethodResults(m, methodReport);

			// Add information about plausibility check
			if (m.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_VALUE)) {
				String value = m.getAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_VALUE).getValue();
				String plausibilityCheckResult =
						(m.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_FAILED)
								? this.translate("report.pdf.method.column.size_check.failed")
								: this.translate("report.pdf.method.column.size_check.passed"))
								+ " (D<sub>50</sub> = " + value + "nm)";
				methodReport.addRow(this.translate("report.pdf.method.column.size_check"),
						plausibilityCheckResult);
			}

			// Add warning if technique is unsuitable
			if (!techniqueSuitabilityMap.get(m.getTechnique().getSignifier())
					.equals(MaterialFeedbackController.SUITABILITY_YES)) {
				this.addWarning(methodReport, this.translate("report.pdf.method.technique.unsuitable"));
			}

			this.addTechniqueHashWarning(methodReport, m.getTechnique(), materials);

			this.addTechniqueWarnings(methodReport, m.getTechnique(), techniqueGeneralWarnings,
					techniqueMaterialWarnings);

			methodReports.add(methodReport);
			i++;
		}

		methodReports.get(0).setPageBreakBefore(true);

		return methodReports;
	}

	/**
	 * Provides model attributes needed during report creation.
	 */
	private void provideModelAttributes(DynamicPdfReport reportSummary) {
		Report report = reportSummary.getReport();

		MaterialService ms = this.serviceManager.getBean(MaterialService.class);
		MaterialTransactionalService mts
				= this.serviceManager.getBean(MaterialTransactionalService.class);
		MaterialCriterionService mcs = this.serviceManager.getBean(MaterialCriterionService.class);
		TechniqueService ts = this.serviceManager.getBean(TechniqueService.class);

		// Collect relevant information

		// Filter techniques
		this.techniquesFiltered = ts.filterTechniquesForDossier(this.techniques,
				report.getDossier());

		this.techniquesFiltered.sort(this.techniqueTierComparator.thenComparing(
				new TechniqueNameComparator()));

		List<Material> materials =
				mts.loadNotArchivedDossierMaterials(report.getDossier());

		Map<String, String> techniqueGeneralWarnings =
				ts.createTechniqueGeneralWarningsMap(this.techniquesFiltered);

		Table<String, Attribute, List<Material>> aggregatedTechniqueMaterialWarningsTable =
				ts.createAggregatedTechniqueMaterialWarnings(this.techniquesFiltered,
						materials);

		Map<String, String> techniqueSuitabilityMap = ms.createTechniqueSuitabilityMap(materials);

		// Unavailable techniques based on user profile settings
		List<Technique> unavailableTechniques =
				ts.loadUnavailableTechniquesForUser(report.getDossier().getUser());

		Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>
				compatibilityTableMap =
				mcs.createMaterialCriteriaTechniqueCompatibilityTableMap(materials);

		Table<Material, String, Double> materialIncompletenessTable =
				ms.createMaterialIncompletenessTable(materials);
		Map<String, List<Double>> techniqueIncompletenessMap =
				ts.createTechniqueIncompletenessMap();

		reportSummary.addModelAttribute("techniqueGeneralWarningsMap",
				techniqueGeneralWarnings)
				.addModelAttribute("techniqueMaterialWarningsTable",
						aggregatedTechniqueMaterialWarningsTable)
				.addModelAttribute("unavailableTechniques", unavailableTechniques)
				.addModelAttribute("materialCriteriaTechniqueCompatibilityTableMap",
						compatibilityTableMap)
				.addModelAttribute("techniqueIncompletenessMap", techniqueIncompletenessMap)
				.addModelAttribute("techniqueSuitabilityMap", techniqueSuitabilityMap)
				.addModelAttribute("materialIncompletenessTable", materialIncompletenessTable)
				.addModelAttribute("materials", materials);
	}

	/**
	 * Add dossier sample information.
	 */
	private List<DynamicPdfReport> sampleInfo(DynamicPdfReport summaryPdfReport) {
		Dossier dossier = summaryPdfReport.getReport().getDossier();
		DynamicPdfReport pdfReport = new DynamicPdfReport();
		pdfReport.addTitle(
				this.cs.createHeadlineSub(this.translate("report.pdf.dossier.sample_heading")),
				this.cs.createHeadlineFiller());

		User user = dossier.getUser();

		this.cs.createStringTable(pdfReport);
		pdfReport.addRow(this.translate("report.pdf.dossier.sample.column.sample"),
				dossier.getSampleName())
				.addRow(this.translate("report.pdf.dossier.sample.column.date"),
						this.translateDate(new Date()))
				.addRow(this.translate("report.pdf.dossier.sample.column.institution"), this.institution)
				.addRow(this.translate("report.pdf.dossier.sample.column.person"),
						this.serviceManager.getBean(UserService.class).getUsernameOrTitleFirstLastName(user))
				.addRow(this.translate("report.pdf.dossier.sample.column.multiconstituent"),
						dossier.isMulticonstituent() ?
								this.translate("global.true") : this.translate("global.false"));

		pdfReport.setPageBreakBefore(false);

		return ImmutableList.of(pdfReport);
	}

	/**
	 * Helper method to translate the given string with the given arguments using the report locale.
	 *
	 * @see #REPORT_LOCALE
	 */
	private String translate(String string, Object... args) {
		return this.ts.translate(string, REPORT_LOCALE, args);
	}

	/**
	 * Helper method to translate the given date using the report locale.
	 *
	 * @see #REPORT_LOCALE
	 */
	private String translateDate(Date date) {
		return this.ts.translateDate(date, REPORT_LOCALE);
	}

	/**
	 * Helper method to translate the given double value using the given locale string in the report
	 * locale.
	 *
	 * @see #REPORT_LOCALE
	 */
	private String translateDouble(String string, Double value) {
		return this.ts.translateDouble(string, value, REPORT_LOCALE);
	}
}
