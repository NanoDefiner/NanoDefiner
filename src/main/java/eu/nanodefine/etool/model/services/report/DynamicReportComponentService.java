/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.report;

import java.awt.Color;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.view.report.DynamicPdfReport;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.FillerBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;

/**
 * Service for creating dynamic report components.
 */
@Service
public class DynamicReportComponentService implements IService {

	public static final int DEFAULT_FILLER_HEIGHT = 10;

	public static final int HEADLINE_FILLER_HEIGHT = 20;

	public static final int SUBREPORT_FILLER_HEIGHT = 50;

	private StyleBuilder columnTitleStyle;

	private StyleBuilder headlineMainStyle;

	private StyleBuilder headlineSubStyle;

	private StyleBuilder headlineSubSubStyle;

	private StyleBuilder textStyle;

	/**
	 * Creates a filter with the default filler height.
	 *
	 * @see #createFiller(int)
	 * @see #DEFAULT_FILLER_HEIGHT
	 */
	public FillerBuilder createDefaultFiller() {
		return this.createDefaultFiller(1);
	}

	/**
	 * Creates a filter with default filler hight modified by the given multiplier.
	 *
	 * @param multiplier Will be multiplied with DEFAULT_FILLER_HEIGHT to determine the filler height.
	 * @see #createFiller(int)
	 * @see #DEFAULT_FILLER_HEIGHT
	 */
	public FillerBuilder createDefaultFiller(double multiplier) {
		return this.createFiller((int) Math.round(DEFAULT_FILLER_HEIGHT * multiplier));
	}

	/**
	 * Creates a filler with the given height.
	 *
	 * @see FillerBuilder#setHeight(Integer)
	 */
	public FillerBuilder createFiller(int height) {
		return DynamicReports.cmp.filler().setHeight(height);
	}

	/**
	 * Creates a filler to be inserted after headlines.
	 *
	 * @see #HEADLINE_FILLER_HEIGHT
	 */
	public FillerBuilder createHeadlineFiller() {
		return this.createFiller(HEADLINE_FILLER_HEIGHT);
	}

	/**
	 * Creates a main headline with the given text.
	 *
	 * @see #headlineMainStyle
	 * @see #createStyles()
	 */
	public TextFieldBuilder<String> createHeadlineMain(String text) {
		return DynamicReports.cmp.text(text).setStyle(this.headlineMainStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
	}

	/**
	 * Creates a sub-headline with the given text.
	 *
	 * @see #headlineSubStyle
	 * @see #createStyles()
	 */
	public TextFieldBuilder<String> createHeadlineSub(String text) {
		return DynamicReports.cmp.text(text).setStyle(this.headlineSubStyle);
	}

	/**
	 * Creates a sub-sub-headline with the given text.
	 *
	 * @see #headlineSubSubStyle
	 * @see #createStyles()
	 */
	public TextFieldBuilder<String> createHeadlineSubSub(String text) {
		return DynamicReports.cmp.text(text).setStyle(this.headlineSubSubStyle);
	}

	/**
	 * Wraps the given components in a report without page-breaks, essentially ensuring the there
	 * will be no page-break between the components if possible.
	 */
	public DynamicPdfReport createReportWithoutPageBreak(
			ComponentBuilder<?, ?>... components) {
		DynamicPdfReport report = new DynamicPdfReport();
		report.getJasperReport().setSummarySplitType(SplitType.PREVENT);
		report.getJasperReport().summary(components);

		return report;
	}

	/**
	 * Creates a string representation of the given object that is sure to be non-empty.
	 *
	 * <p>If the string representation of {@code text} is empty, a dash ("–") is returned.</p>
	 */
	public String createString(Object text) {
		return this.createString(text, "–");
	}

	/**
	 * Creates a string representation of the given object, returning the given default text if the
	 * resulting string is empty.
	 */
	public String createString(Object text, String defaultText) {
		String result = text != null ? text.toString() : defaultText;
		return result.equals("") ? defaultText : result;
	}

	/**
	 * Crestes a  default string table for the given report.
	 *
	 * @see #createStringTable(DynamicPdfReport, String[], String[])
	 */
	public DynamicPdfReport createStringTable(DynamicPdfReport report) {
		return this.createStringTable(report, null, null);
	}

	/**
	 * Creates a custom string table for the given report.
	 *
	 * <p>The created columns are stored in the model of the report for later access under the key
	 * "column_" followed by the column field name (i.e. "column_attribute" and "column_value" by
	 * default).</p>
	 *
	 * @param fieldNames Internal names of the column fields, determines the number of columns. If
	 * {@code null}, two columns named "attribute" and "value" will be created.
	 * @param titles Column titles of the table. If none or not enough are given, the column will
	 * get no title.
	 * @see DRDataSource#DRDataSource(String...)
	 * @see TextColumnBuilder
	 * @see DynamicPdfReport#getModelAttribute(String)
	 */
	public DynamicPdfReport createStringTable(DynamicPdfReport report,
			String[] fieldNames, String[] titles) {

		if (fieldNames == null) {
			fieldNames = new String[] { "attribute", "value" };
		}

		DRDataSource dataSource = new DRDataSource(fieldNames);
		// Highlight even rows for better readability
		report.getJasperReport().setDataSource(dataSource).highlightDetailEvenRows();

		String title;
		TextColumnBuilder<String> column;
		for (int i = 0; i < fieldNames.length; i++) {

			title = (titles != null && titles.length > i) ? titles[i] : "";

			column = DynamicReports.col.column(title, fieldNames[i], DynamicReports.type.stringType());

			// Add column to report model for later access
			report.addModelAttribute("column_" + fieldNames[i], column);

			report.getJasperReport().columns(column);
		}

		if (titles != null) {
			report.getJasperReport().setColumnTitleStyle(this.columnTitleStyle);
		}

		return report;
	}

	/**
	 * Initialize the various styles.
	 */
	@PostConstruct
	private void createStyles() {
		StyleBuilder baseStyle = DynamicReports.stl.style().setFont(DynamicPdfReport.DEFAULT_FONT);
		StyleBuilder boldStyle = DynamicReports.stl.style(baseStyle).bold();
		StyleBuilder boldCenteredStyle = DynamicReports.stl.style(boldStyle)
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
		this.headlineMainStyle = DynamicReports.stl.style(boldCenteredStyle)
				.setVerticalTextAlignment(VerticalTextAlignment.MIDDLE).setFontSize(16);
		this.headlineSubStyle = DynamicReports.stl.style(boldStyle).setFontSize(14);
		this.headlineSubSubStyle = DynamicReports.stl.style(baseStyle).setFontSize(12);
		this.textStyle = DynamicReports.stl.style(baseStyle).setFontSize(10);
		this.columnTitleStyle = DynamicReports.stl.style(boldCenteredStyle).setPadding(3)
				//.setBorder(DynamicReports.stl.pen1Point())
				.setBackgroundColor(Color.LIGHT_GRAY);
	}

	/**
	 * Creates a filler to be inserted after sub-reports.
	 *
	 * @see #SUBREPORT_FILLER_HEIGHT
	 */
	public FillerBuilder createSubreportFiller() {
		return this.createFiller(SUBREPORT_FILLER_HEIGHT);
	}

	/**
	 * Creates a text field with the given text and markup.
	 */
	public TextFieldBuilder<String> createText(String text, Markup markup) {
		return DynamicReports.cmp.text(text).setStyle(this.textStyle)
				.setMarkup(markup);
	}

	/**
	 * Creates a text field with the given text and a default markup.
	 *
	 * <p>The default markup is {@link Markup#STYLED}</p>
	 */
	public TextFieldBuilder<String> createText(String text) {
		return this.createText(text, Markup.STYLED);
	}

	/**
	 * Returns the default column title style for string tables.
	 */
	public StyleBuilder getColumnTitleStyle() {
		return this.columnTitleStyle;
	}

	/**
	 * Returns the default main headline style.
	 */
	public StyleBuilder getHeadlineMainStyle() {
		return this.headlineMainStyle;
	}

	/**
	 * Returns the default sub headline style
	 */
	public StyleBuilder getHeadlineSubStyle() {
		return this.headlineSubStyle;
	}

	/**
	 * Returns the default sub-sub headline style
	 */
	public StyleBuilder getHeadlineSubSubStyle() {
		return this.headlineSubSubStyle;
	}

	/**
	 * Returns the default text style.
	 */
	public StyleBuilder getTextStyle() {
		return this.textStyle;
	}
}
