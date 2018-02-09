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
import eu.nanodefine.etool.constants.ReferenceAttributes;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredReference;
import eu.nanodefine.etool.knowledge.configurations.beans.ReferenceCriterion;

/**
 * Loads the configured references using the reference sheet.
 */
public class ReferenceConfiguration {

	private List<ConfiguredReference> entries;

	private InputStream inputStream;

	private Logger log;

	public ReferenceConfiguration(InputStream inputStream) throws IOException {
		this.log = LoggerFactory.getLogger(ReferenceConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	/**
	 * Returns all configured references.
	 */
	public List<ConfiguredReference> getEntries() {
		return this.entries;
	}

	/**
	 * Returns the first entry for the given reference signifier.
	 */
	public ConfiguredReference getEntry(String referenceSignifier) {
		assert referenceSignifier != null : "null not allowed";

		String ts;
		for (ConfiguredReference cp : this.entries) {
			ts = cp.getEntry(ReferenceAttributes.REFERENCE_SIGNIFIER)
					.getValue();

			if (referenceSignifier.equals(ts)) {
				return cp;
			}
		}

		return null;
	}

	/**
	 * Reads the reference sheet.
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
					this.entries.add(i, new ConfiguredReference());
				}
			}

			// Set attribute and value for each configuration column
			for (Integer i = 0; i < capacity; i++) {
				this.entries.get(i).add(
						new ReferenceCriterion(row[0], row[i + offset]));
			}
		}

		// Print debug information
		for (ConfiguredReference p : this.entries) {
			for (ReferenceCriterion c : p.getEntries()) {
				this.log.debug(c.getName() + " : " + c.getValue());
			}
			this.log.debug(LoggerMessages.BUMPER);
		}

		// Close reader
		reader.close();
	}
}
