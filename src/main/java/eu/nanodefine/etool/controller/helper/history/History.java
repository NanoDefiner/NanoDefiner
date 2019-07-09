/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.helper.history;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Represents the user history within the NanoDefiner application.
 *
 * The history keeps track of the last {@code MAX_ENTRIES} pages the user has
 * visited in the NanoDefiner and provides convenience methods for accessing
 * recently visited pages.
 *
 * @see HistoryEntry
 * TODO findByControllerAndAction?
 *
 * TODO re-evaluate redirect solution
 */
public class History {

	/**
	 * Number of entries stored in the history.
	 */
	private final int MAX_ENTRIES = 10;

	/**
	 * List of history entries.
	 *
	 * @see HistoryEntry
	 */
	private LinkedList<HistoryEntry> historyEntries = new LinkedList<>();

	/**
	 * Contains the last request
	 *
	 * <p>Used to avoid inserting the same request into the history several times.
	 * {@link #getCurrent()} is not used because it is overwritten with the referrer.</p>
	 */
	private String lastRequest;

	private Logger log = LoggerFactory.getLogger(History.class);

	/**
	 * Current redirect value.
	 *
	 * <p>This is either the last page accessed via GET or, if present, the current
	 * {@code redirect} request parameter.</p>
	 *
	 * <p>This value must <em>always</em> start with "redirect:".</p>
	 *
	 * @see eu.nanodefine.etool.controller.interceptors.HistoryInterceptor
	 */
	private String redirect = "redirect:/";

	/**
	 * Creates a new history.
	 *
	 * Please use {@link #forRequest(HttpServletRequest)} to create a new history.
	 */
	private History() {

	}

	/**
	 * Retrieves or creates the history for the session associated with the given request
	 *
	 * <p>The request is registered in the history if it wasn't already.</p>
	 */
	public static History forRequest(HttpServletRequest request) {
		HttpSession session = request.getSession();

		if (session.getAttribute("history") == null) {
			session.setAttribute("history", new History());
		}

		History history = (History) session.getAttribute("history");

		try {
			history.registerRequest(request);
		} catch (URISyntaxException e) {
			history.log.error("Unable to parse request URI: {}", request.getRequestURI());
			history.log.error(e.getMessage());
		}

		request.setAttribute("history", history);

		return history;
	}

	/**
	 * Returns the <code>i</code>th history entry, counting backwards.
	 *
	 * <p><code>i</code> corresponds to the position within the history starting from
	 * the current entry, i.e. for <code>i = 0</code>, {@link #getCurrent()} is
	 * returned.</p>
	 *
	 * @param i Position in the history, starting from the current entry
	 * @return i-th element from the end of the history
	 */
	public HistoryEntry get(int i) {
		return this.historyEntries.get(this.historyEntries.size() - 1 - i);
	}

	/**
	 * Returns the latest history entry.
	 *
	 * <p>Careful: Depending on whether the current request has already been processed, this may be
	 * the last or the current entry. To avoid any problems, access the history by calling
	 * {@link #forRequest(HttpServletRequest)}.</p>
	 */
	public HistoryEntry getCurrent() {
		return this.historyEntries.getLast();
	}

	/**
	 * Returns a list of history entries.
	 */
	public List<HistoryEntry> getEntries() {
		return this.historyEntries;
	}

	/**
	 * Returns the last history entry for the given entity.
	 */
	public Optional<HistoryEntry> getLastByEntity(String entity) {
		for (int i = this.historyEntries.size() - 2; i >= 0; i--) {
			if (this.historyEntries.get(i).getEntity().equals(entity)) {
				return Optional.of(this.historyEntries.get(i));
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns the last history entry for the given request method.
	 */
	public Optional<HistoryEntry> getLastByRequestMethod(
			RequestMethod requestMethod, boolean includeCurrent) {
		for (int i = this.historyEntries.size() - (includeCurrent ? 1 : 2);
				 i >= 0; i--) {
			if (this.historyEntries.get(i).getRequestMethod().equals(requestMethod)) {
				return Optional.of(this.historyEntries.get(i));
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns the last history entry for the given method and entity.
	 */
	public Optional<HistoryEntry> getLastByRequestMethodAndEntity(
			RequestMethod requestMethod, String entity) {
		for (int i = this.historyEntries.size() - 2; i >= 0; i--) {
			if (this.historyEntries.get(i).getRequestMethod().equals(requestMethod)
					&& this.historyEntries.get(i).getEntity().equals(entity)) {
				return Optional.of(this.historyEntries.get(i));
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns the history entry before the current one, if any.
	 *
	 * <p>The same as for {@link #getCurrent()} applies, make sure the current request is processed
	 * before calling this.</p>
	 */
	public HistoryEntry getPrevious() {
		if (!this.hasPrevious()) {
			throw new RuntimeException("No previous history entries");
		}
		return this.historyEntries.get(this.historyEntries.size() - 2);
	}

	public String getRedirect(String anchor) {
		if (anchor != null && !this.redirect.contains("#")) {
			return this.redirect + anchor;
		}

		return this.redirect;
	}

	public String getRedirect() {
		return this.getRedirect(null);
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	/**
	 * Returns whether a previous entry exists, i.e. if the history has more than one entry.
	 */
	public boolean hasPrevious() {
		return this.historyEntries.size() > 1;
	}

	/**
	 * Registers the given requests in the history.
	 *
	 * <p>Adds the request to the history if it's not the same as the last one, and replaces the
	 * last entry with the referrer to account for JS address changes (to store the UI position).</p>
	 */
	@CanIgnoreReturnValue
	private History registerRequest(HttpServletRequest request) throws URISyntaxException {
		URI uri = new URI(request.getRequestURI().substring(request.getContextPath().length())
				.replace("//", "/"));

		this.log.debug("Registering request: {}, {}", uri.toString(), request.getMethod());

		// Ignore HEAD request
		if (request.getMethod().equals(RequestMethod.HEAD.name())) {
			return this;
		}

		// If the request is exactly the same, ignore it
		if (!this.historyEntries.isEmpty()
				&& uri.toString().equals(this.lastRequest)
				&& request.getMethod().equals(this.getCurrent().getRequestMethod().name())) {
			this.log.debug("Ignoring identical request");
			return this;
		}

		String referrer = request.getHeader("referer"); // sic!

		// Replace last entry with referrer to reflect client-side changes to the URI
		if (!this.historyEntries.isEmpty() && referrer != null && !referrer.equals("")
				&& this.getCurrent().getRequestMethod().equals(RequestMethod.GET)) {
			referrer = referrer.replaceFirst(request.getContextPath(), "");
			RequestMethod requestMethod = this.getCurrent().getRequestMethod();
			HistoryEntry referrerEntry = new HistoryEntry(new URI(referrer), requestMethod.name());

			// Only replace if same action
			if (this.getCurrent().isSameEntityAction(referrerEntry)) {

				this.log.debug("Replacing last entry {} with referrer entry {}",
						this.getCurrent(), referrerEntry);

				this.historyEntries.set(this.historyEntries.size() - 1, referrerEntry);
			}
		}

		this.lastRequest = uri.toString();

		this.historyEntries.add(new HistoryEntry(uri, request.getMethod()));

		// Remove entries from the beginning when hitting max size
		if (this.historyEntries.size() > this.MAX_ENTRIES) {
			this.historyEntries.removeFirst();
		}

		return this;
	}

	/**
	 * Returns a string representation of the history.
	 *
	 * <p>Returns a line-break separated list of history entries along with their request method.</p>
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		for (HistoryEntry e : this.historyEntries) {
			result.append(e.toString()).append(" [").append(e.getRequestMethod().toString())
					.append("]\n");
		}

		return result.toString();
	}
}
