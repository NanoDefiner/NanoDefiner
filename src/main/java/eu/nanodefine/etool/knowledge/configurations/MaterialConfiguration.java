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

import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredMaterial;
import eu.nanodefine.etool.knowledge.dictionaries.MaterialDictionary;

/**
 * Loads the configured materials using the material sheet.
 */
public class MaterialConfiguration {

	private List<ConfiguredMaterial> entries;

	private InputStream inputStream;

	private Logger log;

	public MaterialConfiguration(InputStream inputStream)
			throws FileNotFoundException {
		this.log = LoggerFactory.getLogger(MaterialConfiguration.class);

		this.inputStream = inputStream;
		read();
	}

	public List<ConfiguredMaterial> getEntries() {
		return this.entries;
	}

	public void setEntries(List<ConfiguredMaterial> entries) {
		this.entries = entries;
	}

	/**
	 * Returns the first entry with the specified signifier.
	 *
	 * @param signifier signifier to search for
	 * @return first matching entry, otherwise empty {@link ConfiguredMaterial} instance
	 */
	public ConfiguredMaterial getEntry(String signifier) {
		for (ConfiguredMaterial entry : this.entries) {
			if (entry.getMaterialSignifier().equals(signifier)) {
				return entry;
			}
		}
		return new ConfiguredMaterial();
	}

	/**
	 * Reads the material sheet.
	 */
	private void read() {

		// Create reader and define file structure
		CSVReader reader = new CSVReader(new InputStreamReader(this.inputStream),
				CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, false);

		// Define mapping of columns and class attributes
		Map<String, String> mapping = new HashMap<>();
		mapping.put(MaterialDictionary.COLUMN_MATERIAL_SIGNIFIER,
				CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,
						MaterialDictionary.COLUMN_MATERIAL_SIGNIFIER));
		mapping.put(MaterialDictionary.COLUMN_GROUP,
				MaterialDictionary.COLUMN_GROUP);
		mapping.put(MaterialDictionary.COLUMN_NAME,
				MaterialDictionary.COLUMN_NAME);
		mapping.put(MaterialDictionary.COLUMN_COMMENT,
				MaterialDictionary.COLUMN_COMMENT);

		// Define mapping strategy and bean class
		HeaderColumnNameTranslateMappingStrategy<ConfiguredMaterial> strategy =
				new HeaderColumnNameTranslateMappingStrategy<>();
		strategy.setType(ConfiguredMaterial.class); // deprecated, however, needed
		strategy.setColumnMapping(mapping);

		// Create bean parser and parse CSV to beans
		CsvToBean<ConfiguredMaterial> parser = new CsvToBean<>();
		this.entries = parser.parse(strategy, reader);

		/*log.debug("material dictionary entries: " + inputStream.getPath());
		for (ConfiguredMaterial entry : entries)
			log.debug("{}", entry);*/
	}

}
