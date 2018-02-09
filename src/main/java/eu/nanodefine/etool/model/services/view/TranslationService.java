/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for locale-related processing.
 */
@Component("TR")
public class TranslationService implements IService {

	private final MessageSource resourceBundleMessageSource;

	@Autowired
	public TranslationService(@Qualifier("messageSource") MessageSource resourceBundleMessageSource) {
		this.resourceBundleMessageSource = resourceBundleMessageSource;
	}

	/**
	 * Escapes the given locale string for use as jquery ID.
	 *
	 * It does so by replacing dots (".") with an underscore ("_").
	 */
	public String escape(String localeString) {
		return localeString.replace(".", "_");
	}

	/**
	 * Returns the current locale.
	 */
	public Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}

	/**
	 * Converts a locale string (e.g. "en_GB") to a {@link Locale} object.
	 */
	public Locale getLocaleFromString(String localeString) {
		if (localeString == null) {
			return this.getLocale();
		}

		String[] parts = localeString.split("_");

		switch (parts.length) {
			case 1:
				return new Locale(localeString);
			case 2:
				return new Locale(parts[0], parts[1]);
			default:
				return new Locale(parts[0], parts[1], parts[2]);
		}
	}

	/**
	 * Translates the given string using the given locale and arguments.
	 */
	public String translate(String string, Locale locale, Object... args) {
		String defaultMessage = String.format("%s_%s", string, locale);

		return this.resourceBundleMessageSource.getMessage(string, args, defaultMessage, locale);
	}

	/**
	 * Translates the given string using a context locale.
	 *
	 * @see #translate(String, Locale)
	 */
	public String translate(String string) {
		return this.translate(string, this.getLocale());
	}

	/**
	 * Translates the given string using the given locale.
	 *
	 * @see #translate(String, Locale, Object...)
	 */
	public String translate(String string, Locale locale) {
		return this.translate(string, locale, new Object[] {});
	}

	/**
	 * Translates the given string using the given arguments and a context locale.
	 *
	 * @see #translate(String, Locale, Object...)
	 */
	public String translate(String string, Object... args) {
		return this.translate(string, this.getLocale(), args);
	}

	/**
	 * Translates the given date using a context locale.
	 *
	 * <p>Used in the templates.</p>
	 *
	 * @see #translateDate(Date, Locale)
	 */
	public String translateDate(Date date) {
		return this.translateDate(date, this.getLocale());
	}

	/**
	 * Translates the given date using the given locale.
	 */
	public String translateDate(Date date, Locale locale) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				this.translate("global.date_format", locale), locale);
		return dateFormatter.format(date);
	}

	/**
	 * Translates the given double value using the given pattern and a context locale.
	 *
	 * @see #translateDouble(String, double, Locale)
	 */
	public String translateDouble(String patternId, double value) {
		return this.translateDouble(patternId, value, this.getLocale());
	}

	/**
	 * Translates the given double value using the given pattern and locale.
	 */
	public String translateDouble(String patternId, double value, Locale locale) {
		DecimalFormat myFormatter = new DecimalFormat(this.translate(patternId, locale));
		return myFormatter.format(value);
	}
}
