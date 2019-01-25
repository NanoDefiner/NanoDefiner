/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.analysis.processors;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.opencsv.CSVReader;

import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.utilities.classes.Tuple;

/**
 * Processor for Q0 format analysis files.
 *
 * TODO refactor clean entries and associated methods
 */
public class CMFProcessor extends AbstractAnalysisProcessor {

	private final FileService fileService;

	private List<Tuple<Double, Double>> cleanEntries;

	private List<Tuple<Double, Double>> densityFunction;

	private List<Tuple<Double, Double>> distributionFunction;

	private List<Tuple<Double, Double>> entries;

	private Double medianQ0 = 0.5;

	private Double medianSize = null;

	private Tuple<Double, Double> postMedian = null;

	private Integer postMedianIdx = null;

	private Tuple<Double, Double> preMedian = null;

	private Integer preMedianIdx = null;

	public CMFProcessor(Method method, FileService fileService) {
		super(method);
		this.fileService = fileService;
	}

	/**
	 * Quickly clean duplicate entries.
	 *
	 * TODO Call by reference strikes again... deep copy needed.
	 *
	 * TODO ...remove?
	 */
	private void cleanEntries() {
		Map<Double, Double> map = new LinkedHashMap<>();
		for (Tuple<Double, Double> e : this.entries) {
			map.put(e.getLeft(), e.getRight());
		}
		map.clear();

		this.cleanEntries = new Vector<>();
		for (Double d : map.keySet()) {
			this.cleanEntries.add(new Tuple<>(d, map.get(d)));
		}
	}

	/**
	 * Determines the density function.
	 */
	private void determineDensityFunction() {
		this.densityFunction = new Vector<>();

		int numberOfBins = 20;
		double sizeMin = this.entries.get(0).getLeft(),
				sizeMax = this.entries.get(this.entries.size() - 1).getLeft();
		double binWidth = (sizeMax - sizeMin) / numberOfBins;

		double upperBinBorder, lastDensity = 0.;
		int j = 0, numEntries = this.entries.size();
		for (int i = 0; i < numberOfBins; i++) {
			upperBinBorder = sizeMin + (i + 1) * binWidth;

			while (j < numEntries - 1 && this.entries.get(j).getLeft() < upperBinBorder) {
				j++;
			}

			this.densityFunction.add(new Tuple<>(sizeMin + i * binWidth + binWidth / 2,
					this.entries.get(j).getRight() - lastDensity));
			lastDensity = this.entries.get(j).getRight();
		}
	}

	private void determineDensityFunctionFull() {
		this.densityFunction = new Vector<>();

		double lastDensity = 0.;
		int numEntries = this.entries.size();
		for (int i = 0; i < numEntries; i++) {

			// Skip entries with same density, they also have the same size
			if (this.entries.get(i).getRight().equals(lastDensity)) {
				continue;
			}

			this.densityFunction.add(new Tuple<>(this.entries.get(i).getLeft(),
					this.entries.get(i).getRight() - lastDensity));
			lastDensity = this.entries.get(i).getRight();
		}
	}

	public List<Tuple<Double, Double>> getDensity() {
		return this.densityFunction;
	}

	public List<Tuple<Double, Double>> getDistribution() {
		return this.distributionFunction;
	}

	public List<Tuple<Double, Double>> getEntries() {
		return this.entries;
	}

	@Override
	public void process(String analysisData) throws IOException {
		this.read(this.fileService.getFullPathInAnalysisDirectory(analysisData).toFile());

		if (this.entries.isEmpty()) {
			throw new IOException("There are no entries to process.");
		}

		// Look for pre- and post-median
		Double q0;
		for (Tuple<Double, Double> e : this.entries) {
			q0 = e.getRight();
			if (this.postMedian == null && q0 > this.medianQ0) {
				this.postMedianIdx = this.entries.indexOf(e);
				this.postMedian = e;
				this.preMedianIdx = this.postMedianIdx - 1;
				this.preMedian = this.entries.get(this.preMedianIdx);
			}
		}

		Double preSize = this.preMedian.getLeft();
		Double postSize = this.postMedian.getLeft();
		Double preQ0 = this.preMedian.getRight();
		Double postQ0 = this.postMedian.getRight();

		// Linear interpolation between pre- and post-median
		Double slope = (postQ0 - preQ0) / (postSize - preSize);
		this.medianSize = preSize + (this.medianQ0 - preQ0) * slope;

		// TODO Here we need a well described analysis result
		//analysisResult = new AnalysisResult(medianSize, "nm",
		//medianSize <= 100, "Median (D50)");
		this.result = (double) Math.round(this.medianSize);

		// Density and distribution approximation
		determineDensityFunctionFull();
		//determineDistributionFunction(); // TODO Include again after fix
	}

	/**
	 * TODO Documentation
	 *
	 * TODO Removed due do the call by reference problem at
	 * {@link #cleanEntries()}.
	 *
	 * @return
	 */
	/*
	private void determineDistributionFunction() {
		distributionFunction = new Vector<>();
		List<Tuple<Double, Double>> dummy = new Vector<>(entries);

		for (int i = 0; i < dummy.size(); i++) {
			distributionFunction.add(dummy.get(i));

			// Place median interpolated median in the middle of pre and post
			if (i == preMedianIdx)
				distributionFunction
						.add(new Tuple<Double, Double>(medianSize, medianQ0));
		}
	}
	*/
	private void read(File analysisFile) throws IOException {
		Character separator = '\t';
		CSVReader reader = new CSVReader(new FileReader(analysisFile),
				separator);

		Double size = null;
		Double q0 = null;
		Tuple<Double, Double> t = null;

		String[] csvLn = reader.readNext();
		this.entries = new Vector<>();
		while ((csvLn = reader.readNext()) != null) {
			size = Double.valueOf(csvLn[0].trim());
			q0 = Double.valueOf(csvLn[1].trim());
			t = new Tuple<>(size, q0);
			this.entries.add(t);
		}
		reader.close();

		// Remove potentially existing duplicates
		cleanEntries();
	}

}
