/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.dictionaries.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Represents a performance / material criterion
 *
 * TODO Rename "Criterion"
 */
public class Attribute {

	/**
	 * Type constant for binary values.
	 */
	public static final String TYPE_BINARY = "binary";

	/**
	 * Type constant for decimal values.
	 */
	public static final String TYPE_DECIMAL = "decimal";

	/**
	 * Type constant for intervals.
	 */
	public static final String TYPE_INTERVAL = "interval";

	/**
	 * Type constent for scales.
	 */
	public static final String TYPE_SCALE = "scale";

	/**
	 * Type constant for sets.
	 */
	public static final String TYPE_SET = "set";

	/**
	 * Type constant for strings.
	 */
	public static final String TYPE_STRING = "string";

	/**
	 * Criterion description.
	 */
	private String description = null;

	/**
	 * Disclaimer text.
	 */
	private String disclaimer = null;

	/**
	 * Criterion label.
	 */
	private String label = null;

	/**
	 * Manual link.
	 */
	private String manual = null;

	/**
	 * Criterion name.
	 */
	private String name = null; // TODO Refactor "criterion"

	/**
	 * Map of available options.
	 */
	private Map<String, Option> optionMap = null;

	/**
	 * List of available options.
	 */
	private List<Option> options = null;

	/**
	 * Criterion type, one of the type constants.
	 */
	private String type = null;

	public Attribute() {
		super();
	}

	public Attribute(String attribute, String type, List<Option> options,
			String label, String description, String disclaimer, String manual) {
		super();
		this.name = attribute;
		this.type = type;
		this.options = options;
		this.label = label;
		this.description = description;
		this.disclaimer = disclaimer;
		this.manual = manual;

		if (options != null) {
			this.optionMap = new HashMap<>();
			for (Option o : options) {
				this.optionMap.put(o.getName(), o);
			}
		}
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisclaimer() {
		return this.disclaimer;
	}

	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getManual() {
		return this.manual;
	}

	public void setManual(String manual) {
		this.manual = manual;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the option for the given key, or {@code null} of no such option exists.
	 */
	public Option getOption(String key) {
		return this.optionMap.get(key);
	}

	public Map<String, Option> getOptionMap() {
		return this.optionMap;
	}

	public List<Option> getOptions() {
		return this.options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	/**
	 * Returns a subset of the available options for the given string representation of a set.
	 *
	 * @return List of options for the given set of options, or {@code null} if no options are
	 * available, or a list containing {@code null} elements for each non-existent option in the
	 * given set.
	 */
	public List<Option> getOptions(String setString) {
		List<Option> options = null;
		if (this.optionMap != null && setString != null) {
			options = new Vector<>();
			for (String key : ConfigurationUtil.toSet(setString)) {
				options.add(this.optionMap.get(key));
			}
		}
		return options;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
