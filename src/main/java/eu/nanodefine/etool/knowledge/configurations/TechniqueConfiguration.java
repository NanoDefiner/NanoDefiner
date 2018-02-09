/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredTechnique;
import eu.nanodefine.etool.knowledge.dictionaries.TechniqueDictionary;

/**
 * Representation of the file-based technique configuration.
 */
public class TechniqueConfiguration {

	/** Configuration entries. */
	private List<ConfiguredTechnique> entries;

	/**
	 * Configuration file URL.
	 */
	private InputStream inputStream;

	/**
	 * Le logger.
	 */
	private Logger log;

	public TechniqueConfiguration(InputStream inputStream) throws IOException {
		this.log = LoggerFactory.getLogger(TechniqueConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	/**
	 * Returns all configured techniques.
	 */
	public List<ConfiguredTechnique> getEntries() {
		return this.entries;
	}

	public void setEntries(List<ConfiguredTechnique> entries) {
		this.entries = entries;
	}

	/**
	 * Returns the first entry with the specified signifier.
	 *
	 * @param signifier signifier to search for
	 * @return first matching entry, otherwise <code>null</code>
	 */
	public ConfiguredTechnique getEntry(String signifier) {
		for (ConfiguredTechnique entry : this.entries) {
			if (entry.getSignifier().equals(signifier)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Reads the technique sheet.
	 */
	private void read() throws IOException {

		// TODO Use InputStream
		// Create reader and define file structure
		CSVReader reader = new CSVReader(
				new InputStreamReader(this.inputStream),
				CSVParser.DEFAULT_SEPARATOR,
				CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER,
				0, false, false);

		// Define mapping of columns and class attributes
		Map<String, String> mapping = new HashMap<>();
		mapping.put(TechniqueDictionary.COLUMN_TECHNIQUE_SIGNIFIER,
				"signifier");
		mapping.put(TechniqueDictionary.COLUMN_NAME,
				TechniqueDictionary.COLUMN_NAME);
		mapping.put(TechniqueDictionary.COLUMN_COMMENT,
				TechniqueDictionary.COLUMN_COMMENT);

		// Define mapping strategy and bean class
		HeaderColumnNameTranslateMappingStrategy<ConfiguredTechnique> strategy =
				new HeaderColumnNameTranslateMappingStrategy<>();
		strategy.setType(ConfiguredTechnique.class); // deprecated, however, needed
		strategy.setColumnMapping(mapping);

		// Create bean parser and parse CSV to beans
		CsvToBean<ConfiguredTechnique> parser = new CsvToBean<>();
		this.entries = parser.parse(strategy, reader);

		/*log.debug("technique dictionary entries: " + inputStream.getPath());
		for (ConfiguredTechnique ct : entries)
			log.debug("{}", ct);*/
	}

}
