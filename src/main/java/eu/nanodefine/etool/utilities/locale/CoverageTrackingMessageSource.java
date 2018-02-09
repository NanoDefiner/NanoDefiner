/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.locale;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * {@link ReloadableResourceBundleMessageSource} with added locale coverage tracking.
 */
public class CoverageTrackingMessageSource extends CsvMessageSource {

	/**
	 * Tracked locale.
	 */
	private Locale locale = null;

	private Logger log = LoggerFactory.getLogger(CoverageTrackingMessageSource.class);

	/**
	 * Set of codes that could not be resolved.
	 */
	private Set<String> unresolvedLocaleCodes = new HashSet<>();

	/**
	 * Set of codes that have been used.
	 */
	private Set<String> usedLocaleCodes = new HashSet<>();

	/**
	 * Returns the locale for which coverage tracking is enabled.
	 *
	 * This corresponds to the first locale for which a locale code was resolved.
	 */
	public Locale getLocale() {
		return this.locale;
	}

	/**
	 * Resolves the given code in the same way as
	 * {@link ReloadableResourceBundleMessageSource#getMessageInternal(String, Object[], Locale)} but
	 * additionale records the used codes and whether they could be resolved.
	 */
	@Override
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		String result = super.getMessageInternal(code, args, locale);

		if (locale == null) {
			return result;
		}

		if (this.locale == null) {
			this.locale = locale;
			this.log.debug("Selected locale: {}", locale.toString());
		}

		if (!this.locale.equals(locale)) {
			return result;
		}

		int numRequestedCodesBefore = this.usedLocaleCodes.size();
		this.usedLocaleCodes.add(code);

		if (result == null || result.contains(locale.toString())) {
			this.unresolvedLocaleCodes.add(code);
			this.log.warn("Could not resolve locale code \"{}\"", code);
		}

		if (this.usedLocaleCodes.size() > numRequestedCodesBefore) {
			this.logStats();
		}

		return result;
	}

	/**
	 * Returns the overall number of locale codes for the tracked locale.
	 */
	public int getNumLocaleCodes() {
		return this.getMergedProperties(this.locale).getProperties().size();
	}

	/**
	 * Returns the number of unresolved locale codes.
	 */
	public int getNumLocaleCodesUnresolved() {
		return this.unresolvedLocaleCodes.size();
	}

	/**
	 * Returns the number of unused locale codes.
	 */
	public int getNumLocaleCodesUnused() {
		return this.getNumLocaleCodes() - this.getNumLocaleCodesUsed();
	}

	/**
	 * Returns the number of used locale codes.
	 */
	public int getNumLocaleCodesUsed() {
		return this.usedLocaleCodes.size();
	}

	/**
	 * Returns the percentage of used locale codes.
	 */
	public long getPercentLocaleCodesUsed() {
		return Math.round(100. * this.usedLocaleCodes.size() / this.getNumLocaleCodes());
	}

	/**
	 * Returns the set of unresolved locale codes.
	 */
	public Set<String> getUnresolvedLocaleCodes() {
		return this.unresolvedLocaleCodes;
	}

	/**
	 * Returns a set of unused locale codes.
	 *
	 * This will compare the set of used locale codes with the set containing all locale codes
	 * and return the difference.
	 */
	public Set<String> getUnusedLocaleCodes() {
		Set<String> unusedCodes = new HashSet<>();

		for (Object code : this.getMergedProperties(this.locale).getProperties().keySet()) {
			if (!this.usedLocaleCodes.contains(code.toString())) {
				unusedCodes.add(code.toString());
			}
		}

		return unusedCodes;
	}

	/**
	 * Returns the set of used locale codes.
	 */
	public Set<String> getUsedLocaleCodes() {
		return this.usedLocaleCodes;
	}

	/**
	 * Helper method to log locale coverage.
	 */
	private void logStats() {
		this.log.debug("Locale coverage: {}/{} ({}%), unresolved: {}",
				new Object[] { this.usedLocaleCodes.size(), this.getNumLocaleCodes(),
						this.getPercentLocaleCodesUsed(), this.unresolvedLocaleCodes.size() });
	}
}
