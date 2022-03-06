/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultiset;

import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.FileService;

/**
 * Processor for Single Particle Calculation Tool version 2 sheets.
 *
 * @link https://www.wur.nl/en/show/Single-Particle-Calculation-tool.htm
 */
public class SPCTv2Processor extends AbstractAnalysisProcessor {

	/**
	 * Constant for the column in which the particle diameter is stored.
	 */
	private static final int COLUMN_PARTICLE_DIAMETER = 5;

	/**
	 * Constant for the first row to be processed.
	 */
	private static final int FIRST_ROW = 13;

	/**
	 * Size of the SXSSF sliding window.
	 */
	private static final int WINDOW_SIZE = 1000;

	private final FileService fileService;

	/**
	 * Bin width for histogram generation.
	 */
	private double binWidth = .5;

	private Logger log = LoggerFactory.getLogger(SPCTv2Processor.class);

	/**
	 * TODO try SXSSF instead
	 */
	private XSSFSheet sheet;

	private Map<Integer, String> sheetMap;

	private TreeMultiset<Double> values = null;

	public SPCTv2Processor(Method method, FileService fileService) {
		super(method);
		this.fileService = fileService;
	}

	public double getBinWidth() {
		return this.binWidth;
	}

	public XSSFSheet getSheet() {
		return this.sheet;
	}

	/**
	 * Returns a mapping of sheet IDs and names.
	 */
	public Map<Integer, String> getSheetMap() {
		return this.sheetMap;
	}

	/**
	 * Returns the values extracted from the sheet or null if no sheet has been processed yet.
	 */
	public TreeMultiset<Double> getValues() {
		return this.values;
	}

	/**
	 * Processes the given analysis data.
	 *
	 * <p>Analysis data can be:</p>
	 *
	 * <ul>
	 * <li>File name in the analysis directory</li>
	 * <li>{@code null} when the sheet has been initialised previously</li>
	 * </ul>
	 *
	 * @see #process(String, int)
	 */
	@Override
	public void process(String analysisData) throws IOException {

		// If analysisData is null, the file has been loaded before and sheet has been selected
		if (analysisData != null) {
			this.read(this.fileService.getFullPathInAnalysisDirectory(analysisData).toFile());
		}

		// No sheet available? Then we can't do any processing yet, user has to choose sheet first
		if (this.sheet == null) {
			return;
		}

		// Validate sheet
		// TODO Extract numbers into constants
		if (!this.sheet.getRow(10).getCell(0).getStringCellValue().startsWith("INPUT")) {
			throw new RuntimeException("Invalid sheet");
		}

		this.log.trace("Rows in sheet: {}", this.sheet.getPhysicalNumberOfRows());

		this.values = TreeMultiset.create(Comparator.naturalOrder());

		Row row;
		double numValues = this.sheet.getRow(1).getCell(COLUMN_PARTICLE_DIAMETER).getNumericCellValue();
		int rowIndex = FIRST_ROW;
		double value;

		// Collect values
		// TODO what about ignored values, see F2 formula
		while (this.values.size() < numValues) {

			row = this.sheet.getRow(rowIndex);

			// Extract bin and frequency from current row
			try {
				value = row.getCell(COLUMN_PARTICLE_DIAMETER).getNumericCellValue();

				if (value > 0 && value <= 1000) {
					this.values.add((double) Math.round(value));
				}
			} catch (IllegalStateException | NumberFormatException ex) {
				// TODO when does this happend and do we need to deal with it?
				this.log.debug("Exception: {}", ex.getMessage());
			}

			rowIndex++;
		}

		// Abort if no values were found
		if (this.values.size() == 0) {
			throw new RuntimeException("Invalid sheet, no values found");
		}

		double median;
		int medianIndexOdd = this.values.size() / 2, medianIndexEven = medianIndexOdd - 1;
		if (this.values.size() % 2 == 0) {
			// TODO this can be slightly optimized using the advance method
			median = (Iterables.get(this.values, medianIndexOdd) +
					Iterables.get(this.values, medianIndexEven)) / 2;
		} else {
			median = Iterables.get(this.values, medianIndexOdd);
		}

		median = Math.round(median);

		this.log.debug("Median: {}", median);

		this.result = median;

	}

	/**
	 * Processes the given data file and sheet ID.
	 *
	 * <p>This method is called when the file has been loaded but more than one sheet has been found
	 * in the file. This method expects the analysis file name and the sheet chosen by the user.</p>
	 */
	public void process(String dataFile, int sheet) throws IOException {
		FileInputStream fis =
				new FileInputStream(this.fileService.getFullPathInAnalysisDirectory(dataFile).toFile());

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

		this.sheet = myWorkBook.getSheetAt(sheet);

		fis.close();

		this.process(null);
	}

	/**
	 * Loads the given analysis file.
	 */
	private void read(File analysisFile) throws IOException {
		FileInputStream fis = new FileInputStream(analysisFile);

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

		this.sheetMap = new HashMap<>();

		// Find relevant sheets
		for (int i = 0; i < myWorkBook.getNumberOfSheets(); i++) {
			if (myWorkBook.getSheetAt(i).getRow(0).getCell(0).getStringCellValue()
					.equals("SAMPLE FILE")) {
				this.sheetMap.put(i, myWorkBook.getSheetName(i));
			}
		}

		if (this.sheetMap.size() == 0) {
			throw new RuntimeException("Invalid file");
		}

		// Only one sheet? Otherwise user will have to choose first
		if (this.sheetMap.size() == 1) {
			this.sheet = myWorkBook.getSheetAt(this.sheetMap.keySet().iterator().next());
		}

		fis.close();
	}
}
