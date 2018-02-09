/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.view;

import java.util.Map;

import org.springframework.stereotype.Service;

import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for numeric processing used in templates.
 *
 * TODO rename to NumberTemplateService?
 */
@Service("N")
public class NumberService implements IService {

	/**
	 * Formats the given size (bytes) into kB/MB/GB as appropriate.
	 *
	 * <p>The unit will be chosen so that the resulting value is smaller than 1000 if possible. For
	 * MB, one decimal place is displayed, and for GB, two decimal places are displayed, none
	 * otherwise.</p>
	 */
	public String formatFileSize(long size) {
		String[] units = new String[] { "Bytes", "kB", "MB", "GB" };
		Integer[] digits = new Integer[] { 0, 0, 1, 2 };
		double sizeDouble = size;

		int unitIndex = 0;

		while (unitIndex < units.length && sizeDouble > 1000) {
			sizeDouble /= 1000;
			unitIndex++;
		}

		double factor = Math.pow(10, digits[unitIndex]);

		return this.formatNumber(Math.round(sizeDouble * factor) / factor, digits[unitIndex]) + " " +
				units[unitIndex];
	}

	/**
	 * Formats the given number with the given number of decimal places.
	 */
	public String formatNumber(Number num, int numPlaces) {
		return String.format("%." + numPlaces + "f", num.floatValue());
	}

	/**
	 * Formats the given number with the guven number of decimal places, considering the special
	 * cases.
	 *
	 * <p>If the result is on of the keys in the special cases map, the corresponding key is returned
	 * instead of the result.</p>
	 */
	public String formatNumber(Number num, int numPlaces, Map<String, String> specialCases) {
		String result = this.formatNumber(num, numPlaces);

		for (Map.Entry<String, String> entry : specialCases.entrySet()) {
			if (result.equals(entry.getKey())) {
				return entry.getValue();
			}
		}

		return result;
	}

	/**
	 * Formats the given number (between 0 and 1) as percentage including the percentage sign.
	 *
	 * @see #formatPercentage(Number, boolean)
	 */
	public String formatPercentage(Number n) {
		return this.formatPercentage(n, false);
	}

	/**
	 * Formats the given number (between 0 and 1) as percentage with or without a percentage sign.
	 */
	public String formatPercentage(Number n, boolean numberOnly) {
		return Math.round(n.doubleValue() * 100) + (numberOnly ? "" : "%");
	}
}
