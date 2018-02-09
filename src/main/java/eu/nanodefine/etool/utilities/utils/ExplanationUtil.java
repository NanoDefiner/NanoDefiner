/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDOMap;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.services.KnowledgeService;

/**
 * Util for provision of qualitative and quantitative explanation in form of
 * snippets, measures and combination of both.
 *
 * TODO convert to service
 */
public abstract class ExplanationUtil {

	// TODO Replace constants with locale strings
	private static final String dissuasionPrefix = "Dissuaded for: ";

	private static final String htmlBreak = "<br/>";

	private static final String logBreak = "\n";

	private static final String noDissuasion = "This measurement technique supports any possible options.";

	private static String addBreak(String text, boolean html) {
		return text + getBreak(html);
	}

	private static String getBreak(boolean html) {
		return (html ? htmlBreak : logBreak);
	}

	/**
	 * Determines dissuasion for a binary type {@link PerformanceCriterionDDO}.
	 *
	 * By default the dissuasion is determined for an implication.
	 *
	 * Exceptional cases are included.
	 *
	 * @param pddo The {@link PerformanceCriterionDDO}
	 * @return Dissuaded boolean option
	 */
	public static Boolean getIncompletenessDissuasionForTypeBinary(
			PerformanceCriterionDDO pddo) {

		Boolean b = ConfigurationUtil.toBinary(pddo.getValue());
		if (b == null) {
			return null; // Incompleteness on technique side
		}

		if (PerformanceAttributes.ELECTRON_BEAM.equals(pddo.getName())) {
			/*
			 *  Sensitivity exception: electron_beam
			 *  If the technique uses electron beam, then it is dissuaded given
			 *  that the material is sensitive to electron beam.
			 */
			if (b) {
				return Boolean.TRUE;
			}
		} else if (PerformanceAttributes.OS_VACUUM.equals(pddo.getName())) {
			/*
			 *  Sensitivity exception: os_vacuum
			 *  If the technique uses electron beam, then it is dissuaded given
			 *  that the material is sensitive to electron beam.
			 */
			if (b) {
				return Boolean.TRUE;
			}
		} else {
			/*
			 * If technique does NOT support a property, then the technique is
			 * dissuaded given that the material incorporates the property.
			 */
			if (!b) {
				return Boolean.TRUE;
			}
		}

		return null;
	}

	/**
	 * Not used at the moment (0.9.6).
	 *
	 * TODO Documentation
	 *
	 * TODO Implementation
	 *
	 * TODO Exceptional cases
	 *
	 * @param pddo
	 * @return
	 */
	public static Double getIncompletenessDissuasionForTypeDecimal(
			PerformanceCriterionDDO pddo) {

		return null;
	}

	/**
	 * Generates dissuasion for an interval type {@link PerformanceCriterionDDO}.
	 *
	 * By default the dissuasion is determined for a strict fit.
	 *
	 * TODO Implementation of exceptional cases?
	 *
	 * @param pddo The {@link PerformanceCriterionDDO}
	 * @return Lower (0, 1) and upper (2, 3) intervals
	 */
	public static Double[] getIncompletenessDissuasionForTypeInterval(
			PerformanceCriterionDDO pddo, Boolean strict) {

		// TODO strict / partial fit applicable?

		Double[] dissuasion = new Double[4];

		Double[] support = ConfigurationUtil.toInterval(pddo.getValue());

		// Incompleteness on technique side
		if (support == null) {
			return null;
		}

		Double supportLower = support[0];
		Double supportUpper = support[1];

		// Lower dissuasion, lower border: None, unlimited
		dissuasion[0] = null;

		// Lower dissuasion, upper border: 1 or lower support border - 1;
		dissuasion[1] = support[0] - 1;

		// Upper dissuasion, lower border: Upper support border + 1;
		dissuasion[2] = support[1] + 1;

		// Upper dissuasion, upper border: None, unlimited
		dissuasion[3] = null;

		return dissuasion;
	}

	/**
	 * Not used at the moment (0.9.6).
	 *
	 * TODO Documentation
	 *
	 * TODO Implementation
	 *
	 * TODO Exceptional cases
	 *
	 * @param pddo
	 * @return
	 */
	public static String getIncompletenessDissuasionForTypeScale(
			PerformanceCriterionDDO pddo) {

		return null;
	}

	/**
	 * Determines dissuasion for a set type {@link PerformanceCriterionDDO}.
	 *
	 * TODO handle unknown pddo
	 *
	 * @param pddo The performance DDO
	 * @return Set of set option label strings
	 */
	public static Set<String> getIncompletenessDissuasionForTypeSet(
			PerformanceCriterionDDO pddo) {

		Attribute performanceAttr = pddo.getPerformanceDictAttr();
		Set<String> scope = performanceAttr.getOptionMap().keySet();

		Set<String> dissuasion = new HashSet<>(scope); // Copy scope
		Set<String> support = ConfigurationUtil.toSet(pddo.getValue());
		dissuasion.removeAll(support); // Unsupported options stay

		return dissuasion;
	}

	/**
	 * Not used at the moment (0.9.6).
	 *
	 * TODO Documentation
	 *
	 * TODO Implementation
	 *
	 * TODO Exceptional cases
	 *
	 * @param pddo
	 * @return
	 */
	public static String getIncompletenessDissuasionForTypeString(
			PerformanceCriterionDDO pddo) {

		return null;
	}

	/**
	 * Provides dissuasion string, either raw or as HTML.
	 *
	 * @param pddo The {@link PerformanceCriterionDDO}
	 * @param html <code>true</code> for HTML output
	 * @return The dissuasion string
	 */
	public static String getIncompletenessDissuasionString(
			PerformanceCriterionDDO pddo, Boolean html) {
		switch (pddo.getPerformanceDictAttr().getType()) {
			case Attribute.TYPE_SET:
				return getIncompletenessDissuasionStringForTypeSet(pddo, html);
			case Attribute.TYPE_INTERVAL:
				return getIncompletenessDissuasionStringForTypeInterval(pddo, html);
			case Attribute.TYPE_BINARY:
				return getIncompletenessDissuasionStringForTypeBinary(pddo, html);
			case Attribute.TYPE_STRING:
				return getIncompletenessDissuasionStringForTypeString(pddo, html);
			case Attribute.TYPE_DECIMAL:
				return getIncompletenessDissuasionStringForTypeDecimal(pddo, html);
			case Attribute.TYPE_SCALE:
				return getIncompletenessDissuasionStringForTypeScale(pddo, html);
			default:
				return "";
		}
	}

	/**
	 * Generates dissuasion string for an binary type
	 * {@link PerformanceCriterionDDO}.
	 *
	 * Exceptional cases are included.
	 *
	 * @param pddo The {@link PerformanceCriterionDDO}
	 * @return The dissuasion string
	 */
	private static String getIncompletenessDissuasionStringForTypeBinary(
			PerformanceCriterionDDO pddo, Boolean html) {

		String dissuasionString = ((html) ? htmlBreak : logBreak) + dissuasionPrefix;

		Boolean b = getIncompletenessDissuasionForTypeBinary(pddo);
		if (b == null) {
			dissuasionString = "";
			if (!(pddo.isUnknown() || pddo.isIrrelevant())) {
				dissuasionString = ((html) ? htmlBreak : logBreak) + noDissuasion;
			}

		} else {
			String label = KnowledgeService.getReferenceDictionary()
					.getAttribute(pddo.getName()).getLabel();

			dissuasionString += ((html) ? htmlBreak : logBreak)
					+ "Given that the material incorporates "
					+ label.toLowerCase() + " (option: '"
					+ ConfigurationUtil.toBinaryString(b)
					+ "').";
		}

		return dissuasionString;
	}

	private static String getIncompletenessDissuasionStringForTypeDecimal(
			PerformanceCriterionDDO pddo, Boolean html) {
		String dissuasion = ((html) ? htmlBreak : logBreak) + dissuasionPrefix;

		return "";
	}

	/**
	 * Generates dissuasion string for interval types, either raw or as HTML.
	 *
	 * Contains exceptional cases.
	 *
	 * @param pddo The {@link PerformanceCriterionDDO}
	 * @param html Option for generation of HTML string
	 * @return The dissuasion string
	 */
	private static String getIncompletenessDissuasionStringForTypeInterval(
			PerformanceCriterionDDO pddo, Boolean html) {
		String dissuasionString = getBreak(html)
				+ dissuasionPrefix;

		Double[] dissuasion = getIncompletenessDissuasionForTypeInterval(pddo,
				null);

		dissuasionString += getBreak(html);

		// TODO this needs looking at, does it produce correct results for unknown
		// PDDOs? Can dissuasion be != null if !pddo.isUnknown()?
		if (dissuasion == null) {
			dissuasionString = "";
			if (!(pddo.isUnknown() || pddo.isIrrelevant())) {
				dissuasionString = ((html) ? htmlBreak : logBreak)
						+ noDissuasion;
			}

			return dissuasionString;
		}

		String lowerDissuasionInterval = ConfigurationUtil.toIntervalString(
				dissuasion[0] != null ? Long.toString(Math.round(dissuasion[0])) : "",
				dissuasion[1] != null ? Long.toString(Math.round(dissuasion[1])) : "");
		String upperDissuasionInterval = ConfigurationUtil.toIntervalString(
				dissuasion[2] != null ? Long.toString(Math.round(dissuasion[2])) : "",
				dissuasion[3] != null ? Long.toString(Math.round(dissuasion[3])) : "");

		/*
		 * Exceptional cases:
		 * - wr_size_range : Needs to be handled as strict fit attribute.
		 * - analysis_temperature : Needs to be handled as partial fit attribute.
		 */
		if (PerformanceAttributes.WR_SIZE_RANGE.equals(pddo // wr_size_range
				.getPerformanceDictAttr().getName())) {
			dissuasionString +=
					"Particle size ranges that partially or completely lie in "
							+ lowerDissuasionInterval + " or "
							+ upperDissuasionInterval + ".";
		} else if (PerformanceAttributes.ANALYSIS_TEMPERATURE
				.equals(pddo // analysis_temperature
						.getPerformanceDictAttr().getName())) {
			dissuasionString += "Stable temperature ranges that do not intersect "
					+ pddo.getValue() + ".";
		} else { // Defaults

			dissuasionString += "Ranges that partially or completely lie in "
					+ lowerDissuasionInterval + " or "
					+ upperDissuasionInterval + ".";
		}

		return dissuasionString;
	}

	private static String getIncompletenessDissuasionStringForTypeScale(
			PerformanceCriterionDDO pddo, Boolean html) {
		String dissuasion = ((html) ? htmlBreak : logBreak) + dissuasionPrefix;

		return "";
	}

	/**
	 * Generates dissuasion string for set types, either raw or as HTML.
	 *
	 * @param pddo The performance DDO
	 * @param html Option for generation of HTML string
	 * @return The dissuasion string
	 */
	private static String getIncompletenessDissuasionStringForTypeSet(
			PerformanceCriterionDDO pddo, Boolean html) {
		String dissuasionString = getBreak(html) + dissuasionPrefix;

		Attribute performanceAttr = pddo.getPerformanceDictAttr();
		Set<String> dissuasion = getIncompletenessDissuasionForTypeSet(pddo);
		for (String d : dissuasion) {
			dissuasionString = addBreak(dissuasionString, html)
					+ "- " + performanceAttr.getOption(d).getLabel();
		}

		if (dissuasion.isEmpty()) {
			dissuasionString = getBreak(html) + noDissuasion;
		}

		return dissuasionString;
	}

	private static String getIncompletenessDissuasionStringForTypeString(
			PerformanceCriterionDDO pddo, Boolean html) {
		String dissuasion = ((html) ? htmlBreak : logBreak) + dissuasionPrefix;

		String s = getIncompletenessDissuasionForTypeString(pddo);

		return "";
	}

	/**
	 * Returns {@code false} if the technique supports all possible material
	 * attribute values, {@code true} otherwise.
	 */
	public static boolean hasIncompletenessDissuasion(PerformanceCriterionDDO pddo) {
		switch (pddo.getPerformanceDictAttr().getType()) {
			case Attribute.TYPE_SET:
				return !getIncompletenessDissuasionForTypeSet(pddo).isEmpty();
			case Attribute.TYPE_INTERVAL:
				return getIncompletenessDissuasionForTypeInterval(pddo, null) != null;
			case Attribute.TYPE_BINARY:
				return getIncompletenessDissuasionForTypeBinary(pddo) != null;
			case Attribute.TYPE_STRING:
				return getIncompletenessDissuasionForTypeString(pddo) != null;
			case Attribute.TYPE_DECIMAL:
				return getIncompletenessDissuasionForTypeDecimal(pddo) != null;
			case Attribute.TYPE_SCALE:
				return getIncompletenessDissuasionForTypeScale(pddo) != null;
			default:
				return false;
		}
	}

	/**
	 * Determines the incompleteness potential of a material.
	 *
	 * @param material The material
	 * @return The incompleteness potential of the material
	 */
	public static Double materialIncompletenessPotential(
			MaterialCriterionDDOMap material) {
		assert material != null : "null not allowed";
		assert material.size() != 0 : "0 elements not allowed";

		List<String> blacklist = ImmutableList.of("material_signifier");

		Double cardinality = new Double(material.size() - blacklist.size());
		Double sum = 0d;
		for (MaterialCriterionDDO mddo : material.values()) { // accumulate
			if (mddo.isUnknown() && !blacklist.contains(mddo.getName())) {
				sum++;
			}
		}

		Double potential = sum / cardinality;
		return potential;
	}

	/**
	 * Determines the incompleteness potential of a technique.
	 *
	 * @param performance The performance profile of the technique
	 * @return The incompleteness potential
	 */
	public static Double techniqueIncompletenessPotential(
			PerformanceCriterionDDOMap performance) {
		assert performance != null : "null not allowed";
		assert performance.size() != 0 : "0 elements not allowed";

		Double cardinality = new Double(performance.size());
		Double sum = 0d;
		for (PerformanceCriterionDDO pddo : performance.values()) { // accumulate
			if (pddo.isUnknown()) {
				sum++;
			}
		}

		Double potential = sum / cardinality;
		return potential;
	}

	/**
	 * Determines the weighted incompleteness potential of a material regarding the
	 * priority profile of the technique.
	 *
	 * @param performance The performance profile
	 * @param material The material
	 * @return Weighted incompleteness potential of the material
	 */
	public static Double weightedMaterialIncompletenessPotential(
			PerformanceCriterionDDOMap performance,
			MaterialCriterionDDOMap material) {
		assert performance != null : "null not allowed";
		assert performance.size() != 0 : "0 elements not allowed";
		assert material != null : "null not allowed";
		assert material.size() != 0 : "0 elements not allowed";

		Double prio; // Dummy
		Double cardinality = new Double(material.size());
		Double weightedSum = 0d;
		for (MaterialCriterionDDO mddo : material.values()) { // accumulate
			if (mddo.isUnknown()) {
				prio = performance.get(mddo.getName()).getPriority();
				weightedSum += 1 * prio;
			}
		}

		Double weightedPotential = weightedSum / cardinality;
		return weightedPotential;
	}

	/**
	 * Determines the weighted incompleteness potential of a technique regarding
	 * the priority profile of the technique.
	 *
	 * @param performance The performance profile of the technique
	 * @return Weighted incompleteness potential of the technique
	 */
	public static Double weightedTechniqueIncomplenessPotential(
			PerformanceCriterionDDOMap performance) {
		assert performance != null : "null not allowed";
		assert performance.size() != 0 : "0 elements not allowed";

		Double prio; // Dummy
		Double cardinality = new Double(performance.size());
		Double weightedSum = 0d;
		for (PerformanceCriterionDDO pddo : performance.values()) { // accumulate
			if (pddo.isUnknown()) {
				prio = performance.get(pddo.getName()).getPriority();
				weightedSum += 1 * prio;
			}
		}

		Double weightedPotential = weightedSum / cardinality;
		return weightedPotential;
	}
}
