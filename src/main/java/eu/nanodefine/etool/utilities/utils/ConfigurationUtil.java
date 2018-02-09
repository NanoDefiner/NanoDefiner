/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.MaterialCriterion;

/**
 * TODO Documentation
 *
 * TODO move to service
 */
@Service
public abstract class ConfigurationUtil {

	public static final String UNKNOWN = "?";

	public static final String IRRELEVANT = "*";

	public static final String SET_BRACKET_LEFT = "{";

	public static final String SET_BRACKET_RIGHT = "}";

	public static final String SET_SEPARATOR = ",";

	public static final String SET_EMPTY = SET_BRACKET_LEFT + SET_BRACKET_RIGHT;

	public static final String BINARY_YES = "yes";

	public static final String BINARY_NO = "no";

	public static final String INTERVAL_BRACKET_LEFT = "[";

	public static final String INTERVAL_BRACKET_RIGHT = "]";

	public static final String INTERVAL_SEPARATOR = ",";

	public static final String INTERVAL_OPEN = "[,]";

	public static final String INTERVAL_OPEN_LEFT = "[,";

	public static final String INTERVAL_OPEN_RIGHT = ",]";

	public static final String STRING_EMPTY = "";

	public static final String SCALE_VERY_POSITIVE = "++";

	public static final String SCALE_POSITIVE = "+";

	public static final String SCALE_MEDIUM = "o";

	public static final String SCALE_NEGATIVE = "-";

	public static final String SCALE_VERY_NEGATIVE = "--";

	/**
	 * Creates a configuration set string out of a string.
	 *
	 * @param s
	 *            a string
	 * @return configuration set string ({a})
	 */
	public static String toSetString(String s) {

		if (UNKNOWN.equals(s)) { // Case: UNKNOWN
			return UNKNOWN; // "?"
		} else if (!STRING_EMPTY.equals(s) // Case: Already set string
				&& SET_BRACKET_LEFT.equals("" + s.charAt(0))
				&& SET_BRACKET_RIGHT.equals("" + s.charAt(s.length() - 1))) {
			return s;
		} else { // Case: Neither unknown

			return SET_BRACKET_LEFT + s + SET_BRACKET_RIGHT; // "{a}"
		}
	}

	/**
	 * Creates a configuration set string out of a string collection.
	 *
	 * @param c
	 *            string collection
	 * @return configuration set string ({a,b,c})
	 */
	public static String toSetString(Collection<String> c) {

		// Case: null or empty
		if (c == null || c.isEmpty()) {
			return SET_EMPTY; // "{}"

			// Case: Only one element
		} else if (c.size() == 1) {
			return toSetString((String) c.toArray()[0]); // "{a}"

			// Case: Contains only ?
		} else if (c.contains(UNKNOWN) && c.size() == 1) { // Paranoia
			return UNKNOWN; // "?"

			// Case: Contains ? but also other elements
		} else if (c.contains(UNKNOWN) && c.size() > 1) { // Paranoia
			// Ignore UNKNOWN when if more than UNKNOWN is selected
			c = new ArrayList<>(c);
			c.remove(UNKNOWN);
		}

		// Return set string
		return SET_BRACKET_LEFT // {
				+ String.join(SET_SEPARATOR, c) // a,b,c
				+ SET_BRACKET_RIGHT; // }
	}

	/**
	 * Creates a configuration interval string out of two string values.
	 *
	 * @param l
	 *            left side
	 * @param r
	 *            right side
	 * @return configuration interval string ([l,r])
	 */
	public static String toIntervalString(String l, String r) {
		if (l == null) {
			l = "";
		}
		if (r == null) {
			r = "";
		}

		return INTERVAL_BRACKET_LEFT // [
				+ l + INTERVAL_SEPARATOR + r // l,r
				+ INTERVAL_BRACKET_RIGHT; // ]
	}

	/**
	 * Creates a two field double array by converting two string values.
	 *
	 * @param l
	 *            left border
	 * @param r
	 *            right border
	 * @return two field double array ([l,r])
	 */
	public static Double[] toInterval(String l, String r) {
		Double ld = (l == null || l.equals(STRING_EMPTY)) ? null : Double
				.valueOf(l);
		Double rd = (r == null || r.equals(STRING_EMPTY)) ? null : Double
				.valueOf(r);
		return new Double[] { ld, rd };
	}

	/**
	 * Creates a two field double array by converting a configuration interval
	 * string.
	 *
	 * @param i
	 *            configuration interval string
	 * @return two field double array ([l,r])
	 */
	public static Double[] toInterval(String i) {
		if (i == null || i.equals(UNKNOWN)) {
			return null;
		}

		// TODO handle invalid string formats?
		String[] borders = i // "[l,r]"
				.substring(1, i.length() - 1) // "l,r"
				.split(INTERVAL_SEPARATOR); // "l","r"
		return toInterval(borders[0], borders[1]);
	}

	/**
	 * Creates a string set by extracting the elements of a configuration set
	 * string.
	 *
	 * @param s
	 *            configuration set string ({a,b,c})
	 * @return string set containing the elements of the given configuration set
	 *         string
	 */
	public static Set<String> toSet(String s) {

		// Case: "?"
		if (UNKNOWN.equals(s)) {
			return Sets.newHashSet("?");
		}

		// Potential empty set
		Set<String> set = new HashSet<>();

		// Case: Non-empty set
		if (!SET_EMPTY.equals(s)) {
			String[] elements = s // "{a,b,c}"
					.substring(1, s.length() - 1) // "a,b,c"
					.split(SET_SEPARATOR); // "a", "b", "c"

			// trim spaces
			for (String e : elements) {
				set.add(e.trim());
			}
		}
		return set;
	}

	/**
	 * Creates a {@link Boolean} out of a configuration binary string.
	 *
	 * @param value
	 *            configuration binary string (yes|no)
	 * @return {@link Boolean} value, <code>true</code> if "yes",
	 *         <code>false</code> if "no", <code>null</code> in "?"
	 */
	public static Boolean toBinary(String value) {

		if (ConfigurationUtil.UNKNOWN.equals(value)) {
			// Case: "?" (equal to null)
			return null;
		} else {
			if (ConfigurationUtil.BINARY_YES.equals(value)) {
				// Case: "yes" (equal to boolean true)
				return Boolean.TRUE;
			} else if (ConfigurationUtil.BINARY_NO.equals(value)) {
				// Case: "no" (equal to boolean false)
				return Boolean.FALSE;
			} else {
				// Case: Paranoia (misconfiguration might lead here)
				return Boolean.FALSE;
			}
		}
	}

	public static String toBinaryString(Boolean value) {
		if (value == null) {
			return "";
		}
		return (value) ? "yes" : "no";
	}

	public static MaterialCriterion createMaterialCriterion(String[] values,
			Attribute attribute) {

		MaterialCriterion mc = new MaterialCriterion();
		mc.setName(attribute.getName());

		String translated = null;
		switch (attribute.getType()) {
			case Attribute.TYPE_BINARY:
			case Attribute.TYPE_DECIMAL: // or
			case Attribute.TYPE_SCALE: // or
			case Attribute.TYPE_STRING: // or
				translated = values[0];
				break;
			case Attribute.TYPE_INTERVAL:
				if (values.length > 1) {
					// TODO check if both values exist
					translated = toIntervalString(values[0], values[1]);
				} else {
					translated = values[0];
				}
				break;
			case Attribute.TYPE_SET:
				translated = toSetString(Arrays.asList(values));
				break;
			default:
				throw new RuntimeException("Unknown attribute type encountered");
		}

		mc.setValue(translated);

		return mc;
	}
}
