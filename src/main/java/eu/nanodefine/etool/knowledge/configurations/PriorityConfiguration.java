/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import eu.nanodefine.etool.constants.LoggerMessages;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPriority;
import eu.nanodefine.etool.knowledge.configurations.beans.PriorityCriterion;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;

/**
 * Loads the configured priorities using the priority sheet.
 */
public class PriorityConfiguration {

	private List<ConfiguredPriority> entries;

	private InputStream inputStream;

	private Logger log;

	public PriorityConfiguration(InputStream inputStream) throws IOException {
		this.log = LoggerFactory.getLogger(PriorityConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	/**
	 * Returns all configured priorities.
	 */
	public List<ConfiguredPriority> getEntries() {
		return this.entries;
	}

	/**
	 * Returns the first entry for the given technique signifier and default material signifier.
	 *
	 * @see #getEntry(String, String)
	 */
	public ConfiguredPriority getEntry(String techniqueSignifier) {
		return getEntry(techniqueSignifier, "default"); // TODO Externalize
	}

	/**
	 * Returns the first entry for the given technique and material signifier.
	 */
	public ConfiguredPriority getEntry(String techniqueSignifier,
			String materialSignifier) {
		assert techniqueSignifier != null &&
				materialSignifier != null : "null not allowed";

		String ts, ms;
		ConfiguredPriority defaultCp = null;
		for (ConfiguredPriority cp : this.entries) {
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

		return defaultCp;
	}

	/**
	 * Reads the priority sheet.
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
					this.entries.add(i, new ConfiguredPriority());
				}
			}

			// Set attribute and value for each configuration column
			for (Integer i = 0; i < capacity; i++) {
				this.entries.get(i).add(
						new PriorityCriterion(row[0], row[i + offset]));
			}
		}

		// Print debug information
		for (ConfiguredPriority p : this.entries) {
			for (PriorityCriterion c : p.getEntries()) {
				this.log.debug(c.getName() + " : " + c.getValue());
			}
			this.log.debug(LoggerMessages.BUMPER);
		}

		// Close reader
		reader.close();

		this.log.info("import of priority configuration was successful");
	}
}
