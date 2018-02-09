/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import java.util.Set;

import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.services.KnowledgeService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Base class for decidable objects, namely material and performance criteria.
 */
public abstract class DroolsDecisionObject implements Decidable {

	/**
	 * Constant for empty set values.
	 */
	public static final String EMPTY = "{}";

	/**
	 * Constant for irrelevant values.
	 */
	public static final String IRRELEVANT = "*";

	/**
	 * Constant for unknown values.
	 */
	public static final String UNKNOWN = "?";

	/**
	 * Associated attribute in the performance dictionary.
	 */
	protected Attribute performanceDictAttr;

	/**
	 * Associated attribute in the reference dictionary.
	 */
	protected Attribute referenceDictAttr;

	/**
	 * Name of this decidable.
	 */
	private String name;

	/**
	 * Value of this decidable.
	 */
	private String value;

	/**
	 * Creates a new DDO based on the given {@link Decidable}.
	 */
	public DroolsDecisionObject(Decidable criterion) {
		assert criterion != null : "criterion was null";
		assert criterion.getName() != null : "name was null";
		assert criterion.getValue() != null : "value was null";

		this.name = criterion.getName();
		this.value = criterion.getValue();

		// TODO pass attributes to constructor and / or re-use decidable attributes?
		this.performanceDictAttr = KnowledgeService.getPerformanceDictionary().getAttribute(this.name);
		this.referenceDictAttr = KnowledgeService.getReferenceDictionary().getAttribute(this.name);
	}

	/**
	 * Creates a new DDO based on the given name and value.
	 */
	public DroolsDecisionObject(String name, String value) {
		assert name != null : "name was null";
		assert value != null : "value was null";

		this.name = name;
		this.value = value;

		this.performanceDictAttr = KnowledgeService.getPerformanceDictionary().getAttribute(name);
		this.referenceDictAttr = KnowledgeService.getReferenceDictionary().getAttribute(name);
	}

	/**
	 * Checks conjuctions of binary value and another
	 * {@link DroolsDecisionObject}'s binary value.
	 *
	 * @param ddo binary {@link DroolsDecisionObject}
	 * @return <code>true</code> if both boolean are <code>true</code>,
	 * otherwise <code>false</code>
	 */
	public Boolean conjuncts(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Boolean inner = ConfigurationUtil.toBinary(this.value);
		Boolean outer = ConfigurationUtil.toBinary(ddo.getValue());

		return inner && outer;
	}

	/**
	 * Checks if set value contains another {@link DroolsDecisionObject}'s
	 * string value.
	 *
	 * @param ddo string {@link DroolsDecisionObject}
	 * @return <code>true</code> if element of set value
	 */
	public Boolean contains(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Set<String> inner = ConfigurationUtil.toSet(this.value);
		String outer = ddo.getValue();

		return inner.contains(outer);
	}

	/**
	 * Checks disjuctions of binary value and another
	 * {@link DroolsDecisionObject}'s binary value.
	 *
	 * @param ddo binary {@link DroolsDecisionObject}
	 * @return <code>true</code> if both boolean are <code>true</code>,
	 * otherwise <code>false</code>
	 */
	public Boolean disjuncts(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Boolean inner = ConfigurationUtil.toBinary(this.value);
		Boolean outer = ConfigurationUtil.toBinary(ddo.getValue());

		return inner || outer;
	}

	/**
	 * TODO Implement cases of open interval sides <br>
	 * TODO Implement cases of unknown interval sides
	 * <p>
	 * Fits an interval value against another Drools
	 * {@link DroolsDecisionObject}'s interval value to evaluate perfect,
	 * partial or no fitting.
	 * <p>
	 * Case 1: Perfect fit<br>
	 * inner: [-5,5]<br>
	 * outer: [-10,10]<br>
	 * Fit: [-5,5]<br>
	 * Results in a true value in both modes, strict and non-strict.
	 * <p>
	 * Case 2: Partial fit, left overhang<br>
	 * inner: [-5,5]<br>
	 * outer: [0,5]<br>
	 * Fit: [0,5]<br>
	 * Results in a false value when called in strict mode, otherwise true.
	 * <p>
	 * Case 3: Partial fit 2, right overhang<br>
	 * inner: [-5,5]<br>
	 * outer: [-5,0]<br>
	 * Fit: [-5,0]<br>
	 * Results in a false value when called in strict mode, otherwise true.
	 * <p>
	 * Case 4: Partial fit 3, two-side overhang<br>
	 * inner: [-10,10]<br>
	 * outer: [-5,5]<br>
	 * Fit: [-5,5]<br>
	 * Results in a false value when called in strict mode, otherwise true.
	 * <p>
	 * Case 5: No fit<br>
	 * inner: [-5,-2] or [2,5]<br>
	 * outer: [-1,1]<br>
	 * Fit: None<br>
	 * Results in a false value in both modes, strict and non-strict.
	 *
	 * @param ddo interval {@link DroolsDecisionObject}
	 * @param strict if <code>true</code>, perfect fit in other interval is
	 * required
	 * @return in strict mode <code>true</code> if perfect fit is detected, in
	 * non-strict mode <code>true</code> is partial fit is detected
	 */
	public Boolean fits(DroolsDecisionObject ddo, Boolean strict) {
		assert ddo != null : "null not allowed";
		assert strict != null : "mode not specified";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		/*
		 * TODO Split interval DDO in two decimal DDOs
		 *
		 * Checks for unknown and irrelevant values will get possible, also
		 */

		Double[] inner = ConfigurationUtil.toInterval(this.value);
		Double[] outer = ConfigurationUtil.toInterval(ddo.getValue());

		if (inner[0] >= outer[0] && inner[1] <= outer[1]) {
			// Case 1: Perfect fit
			return Boolean.TRUE;
		} else if (inner[0] < outer[0] && inner[0] < outer[1]
				&& inner[1] >= outer[0] && inner[1] <= outer[1]) {
			// Case 2: Partial fit, left overhang
			return (strict) ? Boolean.FALSE : Boolean.TRUE;
		} else if (inner[0] >= outer[0] && inner[0] <= outer[1]
				&& inner[1] > outer[0] && inner[1] > outer[1]) {
			// Case 3: Partial fit, right overhang
			return (strict) ? Boolean.FALSE : Boolean.TRUE;
		} else if (inner[0] < outer[0] && inner[0] < outer[1]
				&& inner[1] > outer[0] && inner[1] > outer[1]) {
			// Case 4: Partial fit, two-side overhang
			return (strict) ? Boolean.FALSE : Boolean.TRUE;
		} else {
			// Case 5: No fit
			return Boolean.FALSE;
		}
	}

	public Boolean fitsPartially(DroolsDecisionObject ddo) {
		return fits(ddo, Boolean.FALSE);
	}

	public Boolean fitsStrictly(DroolsDecisionObject ddo) {
		return fits(ddo, Boolean.TRUE);
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Attribute getPerformanceDictAttr() {
		return this.performanceDictAttr;
	}

	public Attribute getReferenceDictAttr() {
		return this.referenceDictAttr;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	/**
	 * Checks if set value contains another {@link DroolsDecisionObject}'s set
	 * value as subset.
	 *
	 * @param ddo set {@link DroolsDecisionObject}
	 * @return <code>true</code> if subset
	 */
	public Boolean hasSubset(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Set<String> inner = ConfigurationUtil.toSet(this.value);
		Set<String> outer = ConfigurationUtil.toSet(ddo.getValue());

		return inner.containsAll(outer);
	}

	/**
	 * Executes the implication {@code inner => outer}.
	 * <p>
	 * Implication scheme<br>
	 * ({@code false} => {@code false}) : {@code true}<br>
	 * ({@code false} => {@code true}) : {@code true}<br>
	 * ({@code true} => {@code false}) : {@code false}<br>
	 * ({@code true} => {@code true}) : {@code true}
	 *
	 * @param ddo binary {@link DroolsDecisionObject}
	 * @return result of implication inner => outer
	 */
	public Boolean implies(DroolsDecisionObject ddo) {
		return implies(ddo, Boolean.FALSE, Boolean.FALSE);
	}

	/**
	 * Executes the implication {@code inner => outer}.
	 * <p>
	 * Implication scheme<br>
	 * ({@code false} => {@code false}) : {@code true}<br>
	 * ({@code false} => {@code true}) : {@code true}<br>
	 * ({@code true} => {@code false}) : {@code false}<br>
	 * ({@code true} => {@code true}) : {@code true}
	 *
	 * @param ddo binary {@link DroolsDecisionObject}
	 * @param invertInner if <code>true</code> inverts inner value
	 * @param invertOuter if <code>true</code> inverts outer value
	 * @return result of implication inner => outer
	 */
	public Boolean implies(DroolsDecisionObject ddo, Boolean invertInner,
			Boolean invertOuter) {
		assert ddo != null : "null not allowed";
		assert invertInner != null && invertOuter != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Boolean inner = ConfigurationUtil.toBinary(this.value);
		Boolean outer = ConfigurationUtil.toBinary(ddo.getValue());

		// TODO prevent nullpointer
		inner = (invertInner) ? !inner : inner;
		outer = (invertOuter) ? !outer : outer;

		return (inner) ? outer : Boolean.TRUE;
	}

	/**
	 * Checks if set value intersects another {@link DroolsDecisionObject}'s set
	 * value.
	 *
	 * @param ddo set {@link DroolsDecisionObject}
	 * @return <code>true</code> if intersection
	 */
	public Boolean intersects(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Set<String> inner = ConfigurationUtil.toSet(this.value);
		Set<String> outer = ConfigurationUtil.toSet(ddo.getValue());

		inner.retainAll(outer);
		return (inner.size() > 0);
	}

	/**
	 * Checks if set value is empty.
	 *
	 * @return <code>true</code> if empty set, <code>false</code> if not
	 */
	public Boolean isEmpty() {
		assert Attribute.TYPE_SET.equals(this.performanceDictAttr.getType()) : "invalid type";

		return EMPTY.equals(this.value) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Checks if value is marked as irrelevant.
	 *
	 * @return <code>true</code> if irrelevant, <code>false</code> if not
	 */
	public Boolean isIrrelevant() {
		return IRRELEVANT.equals(this.value) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Checks if set value is a subset of another {@link DroolsDecisionObject}'s
	 * set value.
	 *
	 * @param ddo set {@link DroolsDecisionObject}
	 * @return <code>true</code> if subset
	 */
	public Boolean isSubset(DroolsDecisionObject ddo) {
		assert ddo != null : "null not allowed";
		// TODO Type check

		// Processing prevention for irrelevance or incompleteness
		if (representsIrrelevanceOrIncompleteness()
				|| ddo.representsIrrelevanceOrIncompleteness()) {
			return Boolean.FALSE;
		}

		Set<String> inner = ConfigurationUtil.toSet(this.value);
		Set<String> outer = ConfigurationUtil.toSet(ddo.getValue());

		return outer.containsAll(inner);
	}

	/**
	 * Checks if value is marked as unknown.
	 *
	 * @return <code>true</code> if unknown, <code>false</code> if not
	 */
	public Boolean isUnknown() {
		return UNKNOWN.equals(this.value) ? Boolean.TRUE : Boolean.FALSE;
	}

	protected Boolean representsIrrelevanceOrIncompleteness() {
		return (isIrrelevant() || isUnknown()) ? Boolean.TRUE : Boolean.FALSE;
	}

}
