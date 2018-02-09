/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.helper.action;

/**
 * Represents one entry of an action box.
 *
 * It consists of a uri and a label message string.
 *
 * All needed texts are derived from the provided label, e.g. for a label of
 * <code>method.read.action.do_something</code>:
 *
 * <ul>
 * <li><code>method.read.action.do_something</code> – the link label</li>
 * <li><code>method.read.action.do_something.description</code> – the action
 * description</li>
 * <li><code>method.read.action.do_something.description.disabled</code> – the
 * action description if the link is {@literal null}</li>
 * </ul>
 */
public class ActionListEntry {

	private final Object[] descriptionVariables;

	private final Object[] labelVariables;

	/**
	 * Disclaimer modal identifier or {@code null} if no disclaimer is to be
	 * displayed.
	 */
	private String disclaimer;

	/**
	 * If set to {@code false}, the action box entry will not be clickable and
	 * alternative locale strings for disabled actions will be displayed.
	 */
	private boolean enabled;

	/**
	 * Locale string for the action box entry label.
	 */
	private String label;

	/**
	 * URI of the action box entry.
	 */
	private String uri;

	public ActionListEntry() {
		this(null, null, true);
	}

	public ActionListEntry(String uri, String label, boolean enabled) {
		this(uri, label, null, enabled);
	}

	public ActionListEntry(String uri, String label) {
		this(uri, label, true);
	}

	public ActionListEntry(String uri, String label, String disclaimer,
			boolean enabled) {
		this(uri, label, disclaimer, enabled, new Object[] {}, new Object[] {});

	}

	public ActionListEntry(String uri, String label, String disclaimer,
			boolean enabled, Object[] labelVariables, Object[] descriptionVariables) {
		this.enabled = enabled;
		this.label = label;
		this.uri = uri;
		this.disclaimer = disclaimer;

		this.labelVariables = labelVariables != null ? labelVariables : new Object[] {};
		this.descriptionVariables =
				descriptionVariables != null ? descriptionVariables : new Object[] {};
	}

	public ActionListEntry(String uri, String label, String disclaimer) {
		this(uri, label, disclaimer, true);
	}

	public Object[] getDescriptionVariables() {
		return this.descriptionVariables;
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

	public Object[] getLabelVariables() {
		return this.labelVariables;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
