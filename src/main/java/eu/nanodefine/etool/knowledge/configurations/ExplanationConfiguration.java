/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.configurations;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredExplanation;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;

/**
 * Loads the configured explanations using the explanation sheet.
 */
public class ExplanationConfiguration {

	private List<ConfiguredExplanation> entries;

	private InputStream inputStream;

	private Logger log;

	public ExplanationConfiguration(InputStream inputStream)
			throws FileNotFoundException {
		this.log = LoggerFactory.getLogger(ExplanationConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	public List<ConfiguredExplanation> getEntries() {
		return this.entries;
	}

	public void setEntries(List<ConfiguredExplanation> entries) {
		this.entries = entries;
	}

	/**
	 * Returns the first entry with the specified signifier.
	 *
	 * @param attribute attribute to search for
	 * @return first matching entry, otherwise <code>null</code>
	 *
	 * TODO null vs. empty ConfiguredExplanation vs. exception?
	 */
	public ConfiguredExplanation getEntry(String attribute) {
		for (ConfiguredExplanation entry : this.entries) {
			if (entry.getAttribute().equals(attribute)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Reads the explanation sheet.
	 *
	 * TODO extract common logic
	 */
	private void read() {
		// Create reader and define file structure
		CSVReader reader = new CSVReader(new InputStreamReader(this.inputStream),
				CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, false);

		// Define mapping of columns and class attributes
		Map<String, String> mapping = new HashMap<>();
		mapping.put(ExplanationDictionary.ATTRIBUTE,
				ExplanationDictionary.ATTRIBUTE);
		mapping.put(ExplanationDictionary.MATCH,
				ExplanationDictionary.MATCH);
		mapping.put(ExplanationDictionary.MISMATCH, ExplanationDictionary.MISMATCH);
		mapping.put(ExplanationDictionary.UNCERTAINTY_MATERIAL,
				CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,
						ExplanationDictionary.INCOMPLETENESS_MATERIAL));
		mapping.put(ExplanationDictionary.UNCERTAINTY_TECHNIQUE,
				CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,
						ExplanationDictionary.INCOMPLETENESS_TECHNIQUE));
		mapping.put(ExplanationDictionary.IRRELEVANCE,
				ExplanationDictionary.IRRELEVANCE);

		// Define mapping strategy and bean class
		HeaderColumnNameTranslateMappingStrategy<ConfiguredExplanation> strategy =
				new HeaderColumnNameTranslateMappingStrategy<>();
		strategy.setType(ConfiguredExplanation.class); // deprecated, however, needed
		strategy.setColumnMapping(mapping);

		// Create bean parser and parse CSV to beans
		CsvToBean<ConfiguredExplanation> parser = new CsvToBean<>();
		this.entries = parser.parse(strategy, reader);

		this.log.debug("explanation sheet entries: " + this.inputStream.toString());
		for (ConfiguredExplanation entry : this.entries) {
			this.log.debug("{}", entry);
		}

		this.log.info("import of explanation configuration was successful");
	}

}
