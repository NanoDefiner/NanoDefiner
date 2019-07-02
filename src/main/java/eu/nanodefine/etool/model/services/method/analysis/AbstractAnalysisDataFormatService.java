/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.MethodAttribute;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.method.MethodService;
import eu.nanodefine.etool.model.services.method.constants.MethodAttributes;
import eu.nanodefine.etool.model.services.method.constants.MethodStates;
import eu.nanodefine.etool.model.services.report.DynamicReportComponentService;
import eu.nanodefine.etool.model.services.technique.TechniqueService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.view.report.DynamicPdfReport;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;

/**
 * Abstract service for analysis result processing.
 */
abstract public class AbstractAnalysisDataFormatService implements IAnalysisDataFormatService {

	@Autowired
	protected DynamicReportComponentService cs;

	@Autowired
	protected FileService fileService;

	@Autowired
	protected ServiceManager serviceManager;

	@Autowired
	protected TranslationService ts;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Adds information on the analysis file to the report.
	 *
	 * <p>Can be used be sub-classes if an analysis file exists.</p>
	 */
	protected void addAnalysisFile(Method method, DynamicPdfReport report) {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		report.addRow(
				this.ts.translate("method.fragment.spicpms_upload.analysis_file_label"),
				this.ts.translate("report.pdf.method.analysis_file.value",
						ms.extractOriginalDataFileName(method)));
	}

	/**
	 * Adds information on the size plausibility check, if one was performed.
	 */
	@Override
	public void addMethodResults(Method method, DynamicPdfReport report) throws IOException {
		if (!method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_VALUE)) {
			this.addMethodResults(method, report, this.determineDecisionKey(method), "D<sub>50</sub>");
		} else if (method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_FAILED)) {
			this.addMethodResults(method, report, "none", this.ts.translate(
					"report.create.pdf.argumentation.size_plausibility_check"));
		}
	}

	/**
	 * Adds common, customizable method result information to the report, including decision and
	 * argumentation.
	 *
	 * <p>Can be used by sub-classes to add method result information to the report.</p>
	 */
	protected void addMethodResults(Method method, DynamicPdfReport report, String decisionKey,
			String argumentation) throws IOException {

		String decision = this.ts.translate("report.create.pdf.decision." + decisionKey);

		report.addRow(this.ts.translate("method.fragment.result.argumentation"),
				argumentation);

		report.addSummary(
				this.cs.createDefaultFiller(),
				DynamicReports.cmp.line(),
				this.cs.createDefaultFiller(.5),
				this.cs.createText("<b>"
						+ this.ts.translate("report.pdf.method.column.decision")
						+ ":</b> " + decision),
				this.cs.createDefaultFiller(.5),
				DynamicReports.cmp.line(),
				this.cs.createDefaultFiller(2));
	}

	/**
	 * Adds method result plots (distribution and density) to the report.
	 *
	 * <p>Can be used by sub-classes that produce plots.</p>
	 */
	protected void addPlots(Method method, DynamicPdfReport report) throws IOException {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		BufferedImage distributionImage = this.cs.removeImageTransparency(ImageIO
				.read(new File(ms.getMethodDataFilePath(method) + ".distribution.png")));
		BufferedImage densityImage = this.cs.removeImageTransparency(ImageIO
				.read(new File(ms.getMethodDataFilePath(method) + ".density.png")));

		ImageBuilder distributionPlot =
				DynamicReports.cmp.image(distributionImage)
						.setDimension(Math.round(distributionImage.getWidth() * .3f),
								Math.round(distributionImage.getHeight() * .3f))
						.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);

		ImageBuilder densityPlot =
				DynamicReports.cmp.image(densityImage)
						.setDimension(Math.round(densityImage.getWidth() * .3f),
								Math.round(densityImage.getHeight() * .3f))
						.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);

		// TODO untangle further
		report.addSummary(this.cs.createReportWithoutPageBreak(

				DynamicReports.cmp.horizontalList(

						DynamicReports.cmp.verticalList(this.cs.createHeadlineSubSub(
								this.ts.translate("method.fragment.file_upload.distribution_plot_label"))
										.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER),
								this.cs.createDefaultFiller(), distributionPlot),

						DynamicReports.cmp.verticalList(this.cs.createHeadlineSubSub(
								this.ts.translate("method.fragment.file_upload.density_plot_label"))
										.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER),
								this.cs.createDefaultFiller(), densityPlot)

				), this.cs.createDefaultFiller()).getSubreport());
	}

	/**
	 * Adds a size plausibility check to the method if needed.
	 *
	 * <p>Size plausibility check needs to be added if</p>
	 *
	 * <ul>
	 * <li>the method has a result,</li>
	 * <li>the result is non-nano,</li>
	 * <li>the technique that was used is tier 1 and not BET,</li>
	 * <li>the method is not yet marked as requiring a size plausibility check,</li>
	 * <li>and no size plausibility check has been performed for the method yet.</li>
	 * </ul>
	 */
	private void addSizePlausibilityCheckIfNeeded(Method method) {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		// Needs size plausibility check?
		if (method.hasResult() && !ms.isBorderline(method)
				&& !ms.isNano(method)
				&& this.serviceManager.getBean(TechniqueService.class)
				.needsPlausibilityCheck(method.getTechnique())
				&& !method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_NEEDED)
				&& !this.isCheckPlausibilityCheckDone(method)) {
			method.getAttributes()
					.add(new MethodAttribute(method, MethodAttributes.SIZE_PLAUSIBILITY_CHECK_NEEDED));
		}
	}

	/**
	 * Checks the size plausibility of the given method's result using the given plausibility check
	 * value.
	 *
	 * <p>The size plausibility check is considered passed if the plausibility check value (i.e. the
	 * result of another method) is not less than or equal to half and not more than or equal to
	 * double the value of first method result.</p>
	 */
	private boolean checkSizePlausibility(Method method, Double plausibilityCheckValue) {

		return !(plausibilityCheckValue <= (method.getNumericResult() / 2) ||
				plausibilityCheckValue >= (method.getNumericResult() * 2));
	}

	/**
	 * Determines the decision key for the given method.
	 *
	 * <p>Essentially, a boolean value is converted to string: nano = "true", non-nano = "false",
	 * borderline = "null", this is then used in a locale string.</p>
	 */
	protected String determineDecisionKey(Method method) {
		MethodService ms = this.serviceManager.getBean(MethodService.class);

		return this.cs.createString(ms.isBorderline(method) ? "null"
				: ms.isNano(method), "null");
	}

	@Override
	public Integer determineState(Method method) {
		if (method.getDataFile() == null) {
			return MethodStates.CREATED;
		}

		if (method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_NEEDED)) {
			return MethodStates.SIZE_PLAUSIBILITY_CHECK;
		}

		if (method.getResult() == null) {
			return MethodStates.ANALYSIS_DATA;
		}

		return MethodStates.FINISHED;
	}

	/**
	 * Returns whether a plausbility check has been performed for the given method.
	 */
	private boolean isCheckPlausibilityCheckDone(Method method) {
		return method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_VALUE);
	}

	/**
	 * Basic implementation of a first method update, adds a size plausibility check if needed.
	 *
	 * <p>This method is called when firest analysis results have been added for a method.</p>
	 *
	 * @see #addSizePlausibilityCheckIfNeeded(Method)
	 */
	@Override
	public void update(Method method, String data, Double uncertainty) throws Exception {
		this.addSizePlausibilityCheckIfNeeded(method);
	}

	/**
	 * Basic implementation of a method update, handles plausibility checks.
	 *
	 * <p>This method is called when further analysis results have been added to a method.</p>
	 *
	 * <p>This method handles plausibility check data, other input can be handled in the
	 * sub-classes' implementation.</p>
	 */
	@Override
	public void update(Method method, String data) throws Exception {
		if (method.hasAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_NEEDED)) {
			Double plausibilityCheckValue = Double.valueOf(data); // TODO handle exceptions
			// TODO format?
			method.getAttributes().add(new MethodAttribute(method,
					MethodAttributes.SIZE_PLAUSIBILITY_CHECK_VALUE, plausibilityCheckValue.toString()));
			method.removeAttribute(MethodAttributes.SIZE_PLAUSIBILITY_CHECK_NEEDED);

			if (!this.checkSizePlausibility(method, plausibilityCheckValue)) {
				method.getAttributes()
						.add(new MethodAttribute(method, MethodAttributes.SIZE_PLAUSIBILITY_CHECK_FAILED));
			}
		} else {
			this.addSizePlausibilityCheckIfNeeded(method);
		}
	}
}
