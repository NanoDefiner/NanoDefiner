/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.dictionaries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import eu.nanodefine.etool.constants.LoggerMessages;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Option;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;
import eu.nanodefine.etool.utilities.utils.ReflectionUtil;

/**
 * Represents and loads performance dictionary information.
 *
 * TODO Refactor by using a super class for dictionary import, as the code is
 * super-redundant to {@link ReferenceDictionary}.
 */
public class PerformanceDictionary {

	// TODO move somewhere else?
	public static String DEFAULT_MATERIAL_SIGNIFIER = "default";

	private Map<String, Attribute> attributeMap;

	private InputStream inputStream;

	private Set<String> knownAttributes = null;

	private Set<String> knownTypes = null;

	private Logger log;

	public PerformanceDictionary(InputStream inputStream) {
		this.log = LoggerFactory.getLogger(PerformanceDictionary.class);

		this.attributeMap = new HashMap<>();

		// Get known attribute and attribute types
		this.knownAttributes = ReflectionUtil.getClassConstants(PerformanceAttributes.class);
		this.knownTypes = ReflectionUtil.getClassConstants(Attribute.class);

		this.inputStream = inputStream;
		read();

		// List all attributes
		list(this.attributeMap.values());

	}

	/**
	 * Re-implementation of {@link String#trim()}.
	 *
	 * TODO remove…
	 */
	private static String removeWrappingWhitespaces(String s) {
		Boolean first = Character.isWhitespace(s.charAt(0));
		Boolean last = Character.isWhitespace(s.charAt(s.length() - 1));

		// Remove first whitespace
		if (first) {
			s = s.substring(1);
		}

		// Remove last whitespace
		if (last) {
			s = s.substring(0, s.length() - 1);
		}

		// Rrrrrecursion! (until there is no more first or last)
		return (first || last) ? removeWrappingWhitespaces(s) : s;
	}

	public Attribute getAttribute(String attributeName) {
		return this.attributeMap.get(attributeName);
	}

	public Set<String> getAttributeNames() {
		return this.attributeMap.keySet();
	}

	public Set<Attribute> getAttributes() {
		return new HashSet<>(this.attributeMap.values());
	}

	/**
	 * Lists all attributes for debugging purpose.
	 */
	private void list(Collection<Attribute> attributes) {
		for (Attribute a : attributes) {
			this.log.debug(LoggerMessages.BUMPER);
			this.log.debug(a.getName() + " : " + a.getType());
			List<String> setValues = new Vector<>();
			for (Option o : a.getOptions()) {
				this.log.debug("    " + o.getName() + "=" + o.getLabel());
				if ("set".equals(a.getType())) {
					setValues.add(o.getName());
				}
			}

			this.log.debug("description: " + a.getDescription());

			if ("set".equals(a.getType())) {
				this.log.debug("set representation: "
						+ ConfigurationUtil.toSetString(setValues));
			}

			this.log.debug("disclaimer: " + a.getDisclaimer());
			this.log.debug("manual: " + a.getManual());
		}
	}

	/**
	 * Loads the perofrmance dictionary.
	 */
	private void read() {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new InputStreamReader(this.inputStream)); // Read CSV

			// Process lines (header is skipped)
			String[] csvLn = reader.readNext();
			this.log.debug("header is skipped");
			for (int lineCount = 2; (csvLn = reader
					.readNext()) != null; lineCount++) {

				// Get attribute, attribute type, options and description
				String csvAttribute = csvLn[0].trim(); // Whitespace prevention
				String csvType = csvLn[1].trim(); // Whitespace prevention
				String csvOpts = csvLn[2]; // Process options first, see below

				// Clean wrapping whitespaces
				// TODO Better use trim?
				String csvLabel = csvLn[3].trim();
				String csvDescription = csvLn[4].trim();
				String csvDisclaimer = csvLn[5].trim();
				String csvManual = csvLn[6].trim();

				// Initialize validators
				Boolean validAttr;
				Boolean validType;
				Boolean validOpts = Boolean.TRUE;
				// Description has functional influence, no validation needed

				// Check attribute and attribute type
				validAttr = this.knownAttributes.contains(csvAttribute);
				if (!validAttr) {
					this.log.warn("line " + lineCount + ", invalid attribute: "
							+ csvAttribute);
				}
				validType = this.knownTypes.contains(csvType);
				if (!validType) {
					this.log.warn("line " + lineCount + ", invalid type: "
							+ csvType);
				}

				// Process attribute options...
				List<Option> options = new Vector<>();
				String[] csvOptLns = csvOpts.split(Option.OPTION_SEPARATOR);
				for (String csvOptLn : csvOptLns) {

					// ...for sets, scales and binaries
					if (Attribute.TYPE_SET.equals(csvType)
							|| Attribute.TYPE_SCALE.equals(csvType)
							|| Attribute.TYPE_BINARY.equals(csvType)) {

						// Check option
						String[] parts = csvOptLn
								.split(Option.KEY_VALUE_SEPARATOR);
						if (parts.length != 2) {
							validOpts = Boolean.FALSE;
							this.log.warn("line " + lineCount
									+ ", invalid option: " + csvOptLn);
							continue;
						}

						// Extract name and label
						String optName = parts[0];
						String optLabel = parts[1];

						// Prevent empty strings
						if ("".equals(optName) || "".equals(optLabel)) {
							continue;
						}

						// Prevent only-whitespaces
						if (optName.matches("^\\s*$")
								|| optLabel.matches("^\\s*$")) {
							continue;
						}

						// Clean wrapping whitespaces
						optName = optName.trim();
						optLabel = removeWrappingWhitespaces(optLabel);
						// TODO Better use trim?

						// Add option to option list
						options.add(new Option(optName, optLabel));

					} else {
						// TODO Process all the other types
					}
				} // for

				// Final validity check
				if (!validAttr || !validType || !validOpts) {
					this.log.warn("line " + lineCount + " is skipped");
				} else {
					Attribute attr = new Attribute(csvAttribute, csvType,
							options, csvLabel, csvDescription, csvDisclaimer,
							csvManual);
					this.attributeMap.put(csvAttribute, attr);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				this.log.warn("could not close csv reader", e);
			}
		}
	}

	public String translateValue(String attributeName, String value) {
		return this.translateValue(attributeName, value, true);
	}

	/**
	 * Translates the given attribute and value.
	 *
	 * TODO Refactor, a more specific translation would be appropriate
	 *
	 * TODO move to service, we have no access to locales here
	 *
	 * @param attributeName
	 * @param value
	 * @return
	 */
	public String translateValue(String attributeName, String value,
			boolean html) {
		String result = "";
		if (ConfigurationUtil.UNKNOWN.equals(value)) {
			result = "Unknown"; // TODO locales
		} else {
			Attribute attribute = getAttribute(attributeName);
			if (attribute != null) {
				if (Attribute.TYPE_BINARY.equals(attribute.getType())) {
					// Binary: get option value
					result = attribute.getOption(value).getLabel();
				} else if (Attribute.TYPE_INTERVAL.equals(attribute.getType())) {
					// Interval: display as given
					result = value;
				} else if (Attribute.TYPE_DECIMAL.equals(attribute.getType())) {
					// Decimal: display as given
					result = value;
				} else if (Attribute.TYPE_SCALE.equals(attribute.getType())) {
					// Scale: display as given
					result = value;
				} else if (Attribute.TYPE_SET.equals(attribute.getType())) {
					// Set: get all options' values, separated
					String optionSeparator = html ? "<br />" : "\n"; // TODO Externalize
					List<Option> options = attribute.getOptions(value);
					if (options.isEmpty()) {
						result = "None"; // TODO locales
					} else {
						StringBuilder resultBuilder = new StringBuilder(result);
						for (Option o : options) {
							// TODO ensure that o is not {@literal null}
							if (o != null) {
								resultBuilder.append(o.getLabel()).append(optionSeparator);
							}
						}

						// Remove last separator
						if (resultBuilder.length() > optionSeparator.length()) {
							resultBuilder = new StringBuilder(resultBuilder.substring(0,
									resultBuilder.length() - optionSeparator.length()));
						}

						result = resultBuilder.toString();
					}
				} else if (Attribute.TYPE_STRING.equals(attribute.getType())) {
					result = value;
				} else {
					// Paranoia
					result = "? (undefined value)";
					this.log.debug("undefined value '{}' detected for attribute "
							+ "'{}'", value, attributeName);
				}
			} else {
				// Paranoia
				result = "? (undefined attribute)";
				this.log.debug("undefined attribute '{}' detected", attributeName);
			}

		}

		return result;
	}

}
