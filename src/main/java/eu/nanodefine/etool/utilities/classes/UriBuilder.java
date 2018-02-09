/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.classes;

import java.io.UnsupportedEncodingException;

import org.springframework.web.util.UriUtils;

import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;

/**
 * Utility class for URI building.
 *
 * URIs built by this class start after the context path.
 */
public class UriBuilder {

	private String anchor = null;

	private String path = "";

	private String prefix = null;

	private String query = "?";

	private UriBuilder() {

	}

	/**
	 * Constructor for entity and action.
	 *
	 * @see #create(String, String)
	 */
	private UriBuilder(String entity, String action) {
		this.path = "/" + entity + "/" + action;
	}

	/**
	 * Constructor including a prefix.
	 */
	private UriBuilder(String entity, String action, String prefix) {
		this(entity, action);

		this.prefix = prefix;
	}

	/**
	 * Creates a new {@link UriBuilder} for the given entity and action.
	 */
	public static UriBuilder create(String entity, String action) {
		return new UriBuilder(entity, action);
	}

	/**
	 * Creates a new {@link UriBuilder} for the given entity, action, and prefix.
	 */
	public static UriBuilder create(String entity, String action, String prefix) {
		return new UriBuilder(entity, action, prefix);
	}

	/**
	 * Creates a new {@link UriBuilder} as a copy of the given one.
	 */
	public static UriBuilder create(UriBuilder uriBuilder) {
		return uriBuilder.copy();
	}

	/**
	 * Add the given entity ID as a path parameter.
	 *
	 * E.g., a {@link eu.nanodefine.etool.model.dto.User} entity with ID 4 would result in a URI like
	 * {@code .../user.id=4/...}.
	 */
	public UriBuilder addEntityId(IDataTransferObject entity) {
		assert entity != null;

		return this.addPathParam(entity.getEntityType() + ".id", entity.getId());
	}

	/**
	 * Add the given entity ID as a query parameter or do nothing if the entity is {@code null}.
	 *
	 * E.g., a {@link eu.nanodefine.etool.model.dto.User} entity with ID 4 would result in a URI like
	 * {@code ...?user.id=4&...}.
	 */
	public UriBuilder addEntityIdQuery(IDataTransferObject entity) {
		// Enable optional inclusion without prior null checking
		if (entity == null) {
			return this;
		}

		return this.addQueryParam(entity.getEntityType() + ".id", entity.getId());
	}

	/**
	 * Add a path parameter with the given name and value.
	 */
	public UriBuilder addPathParam(String parameter, Object value) {
		this.path += "/" + parameter + "=" + value.toString();

		return this;
	}

	/**
	 * Add a path parameter with the given name and with no value.
	 */
	public UriBuilder addPathParam(String parameter) {
		this.path += "/" + parameter;

		return this;
	}

	/**
	 * Add a query parameter with the given name and value.
	 */
	public UriBuilder addQueryParam(String parameter, Object value) {
		StringBuilder parameterBuilder = new StringBuilder(parameter);

		if (value != null) {
			try {
				parameterBuilder.append("=");
				parameterBuilder.append(UriUtils.encodeQueryParam(value.toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// TODO fail harder?
				parameterBuilder.append("error");
			}
		}

		parameterBuilder.append("&");

		this.query += parameterBuilder.toString();

		return this;
	}

	/**
	 * Add a query parameter with the given name and with no value.
	 */
	public UriBuilder addQueryParam(String parameter) {
		return this.addQueryParam(parameter, null);
	}

	/**
	 * Add a redirect query parameter with anchor to the URI.
	 */
	public UriBuilder addRedirectParam(String value, String anchor) {
		return this.addQueryParam("redirect",
				value + (anchor != null ? "#" + anchor : ""));
	}

	/**
	 * Add a redirect query parameter to the URI.
	 */
	public UriBuilder addRedirectParam(String value) {
		return this.addRedirectParam(value, null);
	}

	/**
	 * Add a redirect query parameter from history and with an anchor to the URI.
	 */
	public UriBuilder addRedirectParam(History history, String anchor) {
		return this.addRedirectParam(history.getCurrent().toString(), anchor);
	}

	/**
	 * Add a redirect query parameter from history to the URI.
	 */
	public UriBuilder addRedirectParam(History history) {
		return this.addRedirectParam(history, null);
	}

	/**
	 * Build the URI.
	 */
	public String build() {
		return this.toString();
	}

	/**
	 * Build a Spring redirect from the URI.
	 */
	public String buildRedirect() {
		return "redirect:" + this.toString();
	}

	/**
	 * Copy this URI builder.
	 */
	public UriBuilder copy() {
		UriBuilder copy = new UriBuilder();
		copy.anchor = this.anchor;
		copy.path = this.path;
		copy.query = this.query;

		return copy;
	}

	/**
	 * Returns the anchor of this URI.
	 */
	public String getAnchor() {
		return this.anchor;
	}

	/**
	 * Sets the URI anchor.
	 *
	 * @param anchor URI anchor, not including the #.
	 */
	public UriBuilder setAnchor(String anchor) {
		this.anchor = anchor;

		return this;
	}

	/**
	 * Returns the path component of this URI.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Returns the query component of this URI.
	 */
	public String getQuery() {
		return this.query;
	}

	/**
	 * Builds the URI.
	 */
	public String toString() {
		return (this.prefix != null ? this.prefix : "")
				+ this.path + this.query
				+ (this.anchor != null ? "#" + this.anchor : "");
	}
}
