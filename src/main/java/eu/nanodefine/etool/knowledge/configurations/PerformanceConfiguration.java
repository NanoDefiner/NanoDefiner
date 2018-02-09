/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import eu.nanodefine.etool.constants.LoggerMessages;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.exception.runtime.ConfigurationException;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.configurations.beans.PerformanceCriterion;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;

/**
 * Loads the configured performances using the performance sheet.
 */
public class PerformanceConfiguration {

	private List<ConfiguredPerformance> entries;

	private InputStream inputStream;

	private Logger log;

	public PerformanceConfiguration(InputStream inputStream) throws IOException {
		this.log = LoggerFactory.getLogger(PerformanceConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	/**
	 * Returns all configured performances.
	 */
	public List<ConfiguredPerformance> getEntries() {
		return this.entries;
	}

	/**
	 * Returns all configured performances for the given material signifier.
	 */
	public Collection<ConfiguredPerformance> getEntriesForMaterialSignifier(String materialSignifier) {
		Map<String, ConfiguredPerformance> performances = new HashMap<>(this.entries.size());

		String ts, ms;
		for (ConfiguredPerformance cp : this.entries) {
			ts = cp.getEntry(PerformanceAttributes.TECHNIQUE_SIGNIFIER).getValue();
			ms = cp.getEntry(PerformanceAttributes.MATERIAL_SIGNIFIER).getValue();

			if (ms.equals(materialSignifier)) {
				performances.put(ts, cp);
			}

			performances.putIfAbsent(ts, cp);
		}

		return performances.values();
	}

	/**
	 * Returns all entries for the given technique signifier.
	 */
	public Collection<ConfiguredPerformance> getEntriesForTechniqueSignifier(String techniqueSignifier) {
		List<ConfiguredPerformance> performances = new ArrayList<>();

		String ts;
		for (ConfiguredPerformance cp : this.entries) {
			ts = cp.getEntry(PerformanceAttributes.TECHNIQUE_SIGNIFIER).getValue();

			if (ts.equals(techniqueSignifier)) {
				performances.add(cp);
			}
		}

		return performances;
	}

	/**
	 * Returns the first entry with the given technique and material signifier.
	 *
	 * <p>If no entry for the given material signifier is found, the entry for the default material
	 * signifier is returned instead.</p>
	 *
	 * @throws ConfigurationException if the technique for the given signifier does not exist
	 */
	public ConfiguredPerformance getEntry(String techniqueSignifier,
			String materialSignifier) {
		assert techniqueSignifier != null &&
				materialSignifier != null : "null not allowed";

		String ts, ms;
		ConfiguredPerformance defaultCp = null;
		for (ConfiguredPerformance cp : this.entries) {
			ts = cp.getEntry(PerformanceAttributes.TECHNIQUE_SIGNIFIER).getValue();
			ms = cp.getEntry(PerformanceAttributes.MATERIAL_SIGNIFIER).getValue();

			if (techniqueSignifier.equals(ts)) {

				if (ms.equals(materialSignifier)) {
					return cp;
				} else if (ms.equals(PerformanceDictionary.DEFAULT_MATERIAL_SIGNIFIER)) {
					defaultCp = cp;
				}
			}
		}

		if (defaultCp == null) {
			throw new ConfigurationException(
					"Inconsistency in the knowledge base for technique signifier: " + techniqueSignifier);
		}

		return defaultCp;
	}

	/**
	 * Returns the first entry for the given technique signifier and default material signifier.
	 */
	public ConfiguredPerformance getEntry(String techniqueSignifier) {
		return getEntry(techniqueSignifier, PerformanceDictionary.DEFAULT_MATERIAL_SIGNIFIER);
	}

	/**
	 * Reads the performance sheet.
	 */
	private void read() throws IOException {

		// Read CSV, skip header row
		CSVReader reader = new CSVReader(new InputStreamReader(this.inputStream));
		String[] header = reader.readNext();

		Integer offset = 2; // Skip attribute and example columns
		Integer capacity = header.length - offset;

		// Process attribute rows
		List<String[]> rows = reader.readAll();
		for (String[] row : rows) {

			// Initialize configuration columns
			if (this.entries == null) {
				this.entries = new Vector<>(capacity);
				for (Integer i = 0; i < capacity; i++) {
					this.entries.add(i, new ConfiguredPerformance());
				}
			}

			// Set attribute and value for each configuration column
			for (Integer i = 0; i < capacity; i++) {
				this.entries.get(i).add(
						new PerformanceCriterion(row[0], row[i + offset]));
			}
		}

		// Print debug information
		for (ConfiguredPerformance p : this.entries) {
			for (PerformanceCriterion c : p.getEntries()) {
				this.log.debug(c.getName() + " : " + c.getValue());
			}
			this.log.debug(LoggerMessages.BUMPER);
		}

		// Close reader
		reader.close();
	}
}
