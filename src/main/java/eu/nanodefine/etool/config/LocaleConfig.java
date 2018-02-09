/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import eu.nanodefine.etool.utilities.locale.CoverageTrackingMessageSource;
import eu.nanodefine.etool.utilities.locale.CsvMessageSource;

/**
 * Spring configuration class for locale management.
 */
@Configuration
public class LocaleConfig {

	@Value("${build.date}")
	private String buildDate;

	@Value("${dev.locales}")
	private boolean localesDebug;

	@Value("${locales.directory}")
	private String localesDirectory;

	private Logger log = LoggerFactory.getLogger(LocaleConfig.class);

	@Value("${dev.production}")
	private boolean production;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Value("${build.version}")
	private String version;

	/**
	 * Creates a list of available locales by looking up all locale files.
	 *
	 * @param messageSource Used to express the dependency on the message source. Locale files
	 * may not be available prior to message source initialization.
	 */
	@Bean("availableLocales")
	@Autowired
	public List<Locale> availableLocales(MessageSource messageSource) throws IOException {

		List<Locale> locales = new ArrayList<>();

		File localesDirectory = this.resourceLoader.getResource(this.localesDirectory).getFile();
		FilenameFilter propertiesFilenameFilter =
				(file, name) -> name.toLowerCase().endsWith(".properties");

		String[] fileNameParts;
		Locale.Builder localeBuilder;
		for (File file : localesDirectory.listFiles(propertiesFilenameFilter)) {
			fileNameParts = file.getName().split("\\.")[0].split("_");

			if (fileNameParts.length < 2) {
				continue;
			}

			localeBuilder = new Locale.Builder().setLanguage(fileNameParts[1]);

			if (fileNameParts.length > 2) {
				localeBuilder.setRegion(fileNameParts[2]);
			}

			locales.add(localeBuilder.build());

			this.log.debug("Added locale: {}", localeBuilder.build().getDisplayName());
		}

		return locales;
	}

	/**
	 * Creates empty properties files for each CSV locale file, otherwise the locales won't be
	 * picked up.
	 *
	 * <p>These empty files are needed because locale processing is triggered by the presence of
	 * .properties files for the given locale. Once it has been triggered, the algorithm will
	 * look for .csv files and treat those with priority.</p>
	 */
	private void createEmptyPropertiesFiles() throws IOException {
		File localesDirectory = this.resourceLoader.getResource(this.localesDirectory).getFile();
		FilenameFilter csvFilenameFilter =
				(file, name) -> name.toLowerCase().endsWith(CsvMessageSource.CSV_SUFFIX);

		String[] filenameParts;
		String filenameProperties;
		Resource csvResource;
		for (File file : localesDirectory.listFiles(csvFilenameFilter)) {
			this.log.debug("Processing file: {}", file.getName());
			filenameParts = file.getName().split("\\.");
			filenameProperties = this.localesDirectory + filenameParts[0] + ".properties";

			if (filenameParts.length > 1
					&& filenameParts[1].equals(CsvMessageSource.CSV_SUFFIX.substring(1))
					&& !this.resourceLoader.getResource(filenameProperties).exists()) {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(filenameProperties, false)));
				writer.write("# Do not remove this file, see README.txt");
				writer.newLine();
				this.writeGenerationDate(writer);
				writer.close();
			}
		}
	}

	/**
	 * Provides the default locale.
	 *
	 * TODO make configurable.
	 */
	@Bean("defaultLocale")
	public Locale defaultLocale() {
		return Locale.UK;
	}

	/**
	 * Creates the {@link LocaleResolver} and sets the default locale.
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(defaultLocale());
		return slr;
	}

	/**
	 * Sets up the message source and sets base location and encoding.
	 */
	@Bean
	@Autowired
	public MessageSource messageSource(ResourceLoader resourceLoader) {
		try {
			this.createEmptyPropertiesFiles();
		} catch (Exception e) {
			this.log.warn("Error during locale file preparation:", e);
		}

		ReloadableResourceBundleMessageSource messageSource =
				new CsvMessageSource();

		if (this.localesDebug) {
			messageSource = new CoverageTrackingMessageSource();
		}

		messageSource.setResourceLoader(resourceLoader);
		messageSource.setBasename("file:config/locales/messages");
		// TODO extract into application configuration
		messageSource.setDefaultEncoding("UTF-8");
		// This is needed to allow locale strings to be viewed using ?lang=...
		messageSource.setFallbackToSystemLocale(false);

		this.log.info("Message source created and configured");

		return messageSource;
	}

	/**
	 * Write version and date information into given writer.
	 */
	private void writeGenerationDate(BufferedWriter writer) throws IOException {
		writer.write("# Generated on " + this.buildDate + " using version " + this.version);
		writer.newLine();
	}
}
