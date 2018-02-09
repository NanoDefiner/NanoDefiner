/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.locale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

/**
 * Message source which looks for and priorizes CSV files over .properties files.
 */
public class CsvMessageSource extends ReloadableResourceBundleMessageSource {

	public static final String CSV_SUFFIX = ".csv";

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * TODO documentation
	 */
	@Override
	protected Properties loadProperties(Resource resource, String filename) throws IOException {

		String filenameCsv = filename + CSV_SUFFIX;

		Resource resourceCsv = this.resourceLoader.getResource(filenameCsv);

		// Fall back to normal loading if CSV doesn't exist
		if (!resourceCsv.exists()) {
			this.logger.info("Falling back to normal loading for file: " + filename);
			return super.loadProperties(resource, filename);
		}

		this.logger.info("Reading file: " + filenameCsv);

		InputStream is = resourceCsv.getInputStream();
		// Create reader and define file structure
		CSVReader reader = new CSVReader(new InputStreamReader(is),
				CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER,
				CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, true);

		Properties properties = newProperties();
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			properties.put(nextLine[0], nextLine[1]);
		}

		return properties;
	}
}
