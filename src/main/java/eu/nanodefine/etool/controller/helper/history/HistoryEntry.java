/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.helper.history;

import java.net.URI;

import org.springframework.web.bind.annotation.RequestMethod;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;

/**
 * Represents an entry in the history.
 *
 * <p>A history entry represents a request to the application, containing an entity, action, and
 * parameters, i.e. {@code /contextPath/entity/action/param1=val1?param2=val2}</p>
 *
 * TODO hasQueryParam(), getQueryParam()
 */
public class HistoryEntry {

	/**
	 * Action part of the URI, usually one of the constants in {@link Actions}.
	 */
	private String action;

	/**
	 * Entity part of the URI, usually one of the constants in {@link Entities}.
	 */
	private String entity;

	/**
	 * Path parameters of the URI.
	 *
	 * <p>Path parameters are separated by "/", name and value are separated by "=".</p>
	 */
	private String parameters = "";

	/**
	 * Query parametrers of the URI.
	 */
	// TODO may be null
	private String query;

	/**
	 * Request method of the request, usually {@link RequestMethod#GET} or {@link RequestMethod#POST}.
	 */
	private RequestMethod requestMethod;

	/**
	 * Full request URI.
	 */
	private URI requestUri;

	public HistoryEntry(URI requestUri,
			String requestMethod) {
		this.requestUri = requestUri;
		this.query = this.requestUri.getQuery();
		this.requestMethod = RequestMethod.valueOf(requestMethod);

		// Extract entity and action, if any
		String[] parts = this.requestUri.getPath().split("/");

		this.entity = parts.length > 1 ? parts[1] : Entities.INDEX;
		this.action = parts.length > 2 ? this.sanitizeParam(parts[2]) : Actions.READ;

		// Additional parameters
		if (parts.length > 3) {
			for (int i = 3; i < parts.length; i++) {
				this.parameters += "/" + parts[i];
			}
		}
	}

	public String getAction() {
		return this.action;
	}

	public String getEntity() {
		return this.entity;
	}

	public String getParameters() {
		return this.parameters;
	}

	public String getQuery() {
		return this.query;
	}

	public RequestMethod getRequestMethod() {
		return this.requestMethod;
	}

	public URI getRequestUri() {
		return this.requestUri;
	}

	/**
	 * Returns whether this entry represents the same entity and action as the given entry.
	 */
	public boolean isSameEntityAction(HistoryEntry entry) {
		return this.entity.equals(entry.getEntity()) && this.action.equals(entry.getAction());
	}

	/**
	 * Make sure we don't include stuff in the parameter we don't want.
	 *
	 * <p>This was added to deal with ";JSESSIONID=…" at the end of URLs which was appended by the
	 * server when it wasn't sure whether cookies were accepted or not.</p>
	 */
	private String sanitizeParam(String param) {
		int index = param.indexOf(";");

		if (index != -1) {
			return param.substring(0, index);
		}

		return param;
	}

	/**
	 * Returns a string representation of the request.
	 *
	 * <p>The original URI is reconstructed and the result returned.</p>
	 *
	 * TODO can we just use #requestUri?
	 */
	public String toString() {
		return "/" + this.entity + "/" + this.action + this.parameters +
				(this.query != null ? "?" + this.query : "");
	}
}
