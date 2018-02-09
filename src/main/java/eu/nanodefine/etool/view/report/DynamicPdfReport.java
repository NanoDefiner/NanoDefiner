/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.ModelMap;

import eu.nanodefine.etool.model.dto.Report;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.datasource.DRDataSource;

/**
 * Simple report class on top of the {@link JasperReportBuilder}.
 *
 * <p>Compared to a plain report builder, it adds</p>
 *
 * <ul>
 * <li>a {@link ModelMap} which can be shared between related reports,</li>
 * <li>automatic fonts/style management,</li>
 * <li>and simple sub-report creation.</li>
 * </ul>
 *
 * <p>Additionaly, it allows convenient access to the most relevant underlying methods.</p>
 */
public class DynamicPdfReport {

	public static FontBuilder DEFAULT_FONT = DynamicReports.stl.font().setFontName("DejaVu Serif");

	private Logger log = LoggerFactory.getLogger(DynamicPdfReport.class);

	private ModelMap model;

	private boolean pageBreakBefore = true;

	private JasperReportBuilder reportBuilder;

	public DynamicPdfReport() {
		this(DynamicReports.report());
	}

	/**
	 * Creates a new report using the model of the given report.
	 */
	public DynamicPdfReport(DynamicPdfReport report) {
		this(DynamicReports.report(), report.getModel());
	}

	/**
	 * Creates a new report using the given {@link JasperReportBuilder} with an empty model.
	 */
	public DynamicPdfReport(JasperReportBuilder reportBuilder) {
		this(reportBuilder, new ExtendedModelMap());
	}

	/**
	 * Creates a new report  using the given {@link JasperReportBuilder} and model.
	 */
	public DynamicPdfReport(JasperReportBuilder reportBuilder, ModelMap model) {
		this.reportBuilder = reportBuilder;
		this.model = model;

		// TODO extract default settings

		this.reportBuilder.setDefaultFont(DEFAULT_FONT);
		this.reportBuilder
				.setColumnStyle(DynamicReports.stl.style().setPadding(3).setMarkup(Markup.HTML));
	}

	/**
	 * Add an attribute to the model.
	 *
	 * @see ModelMap#put(Object, Object)
	 */
	public DynamicPdfReport addModelAttribute(String name, Object value) {
		this.model.put(name, value);

		return this;
	}

	/**
	 * Add a row to the {@link DRDataSource} of the report builder of this report.
	 *
	 * @see DRDataSource#add(Object...)
	 */
	public DynamicPdfReport addRow(Object... values) {
		assert this.reportBuilder.getDataSource() != null
				: "Create data source before adding rows";

		((DRDataSource) this.reportBuilder.getDataSource()).add(values);

		return this;
	}

	/**
	 * Add components to the summary of this report.
	 */
	public DynamicPdfReport addSummary(ComponentBuilder<?, ?>... components) {
		this.reportBuilder.summary(components);

		return this;
	}

	/**
	 * Add components to the title of this report.
	 */
	public DynamicPdfReport addTitle(ComponentBuilder<?, ?>... components) {
		this.reportBuilder.title(components);

		return this;
	}

	/**
	 * Returns the {@link TextColumnBuilder} of the column with the given name.
	 */
	public TextColumnBuilder<String> getColumn(String fieldName) {
		return (TextColumnBuilder<String>) this.getModelAttribute("column_" + fieldName);
	}

	/**
	 * Returns the underlying report builder.
	 */
	public JasperReportBuilder getJasperReport() {
		return this.reportBuilder;
	}

	/**
	 * Returns the model.
	 */
	public ModelMap getModel() {
		return this.model;
	}

	/**
	 * Replaces the model with the given one.
	 */
	public void setModel(ModelMap model) {
		this.model = model;
	}

	/**
	 * Returns the model attribute with the given name or {@code null} if it does not exist.
	 */
	public Object getModelAttribute(String name) {
		return this.model.get(name);
	}

	/**
	 * Returns the {@link Report} associated with this PDF report.
	 */
	public Report getReport() {
		assert this.model.containsAttribute("report") : "No report found in the model";
		return (Report) this.model.get("report");
	}

	/**
	 * Creates a {@link SubreportBuilder} for this report.
	 */
	public SubreportBuilder getSubreport() {
		return DynamicReports.cmp.subreport(this.reportBuilder);
	}

	/**
	 * Creates a {@link SubreportBuilder} without page breaks for this report.
	 */
	public SubreportBuilder getSubreportWithoutPageBreaks() {
		DynamicPdfReport wrapper = new DynamicPdfReport(this);
		wrapper.getJasperReport().setSummarySplitType(SplitType.PREVENT);
		wrapper.addSummary(this.getSubreport());

		return wrapper.getSubreport();
	}

	/**
	 * Returns whether there should be a page break before this report when included as a subreport.
	 */
	public boolean isPageBreakBefore() {
		return this.pageBreakBefore;
	}

	/**
	 * Sets whether there should be a page break before this report when included as a subreport.
	 */
	public DynamicPdfReport setPageBreakBefore(boolean pageBreakBefore) {
		this.pageBreakBefore = pageBreakBefore;

		return this;
	}
}
