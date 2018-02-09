/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.view;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.controller.helper.history.HistoryEntry;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.utilities.classes.UriBuilder;

/**
 * Service for URI-related processing mostly used in templates.
 */
@Service("U")
public class UriService implements IService {

	@Value("${server.baseName}")
	private String baseName;

	/**
	 * Builds a material creation URI for the given dossier.
	 *
	 * TODO move to material template service?
	 */
	public UriBuilder buildMaterialCreationUri(Dossier dossier) {
		return this.builder(Entities.MATERIAL, Actions.CREATE).addEntityId(dossier);
	}

	/**
	 * Builds a method creation URI for the given dossier and tier.
	 *
	 * TODO move to method template service?
	 */
	public UriBuilder buildMethodCreationUri(Dossier dossier, String tier) {
		return this.builder(Entities.METHOD, Actions.CREATE)
				.addEntityId(dossier)
				.addPathParam(Entities.METHOD + "." + PerformanceAttributes.TIER, tier);
	}

	/**
	 * Builds a redirection parameter from the given history with the given anchor.
	 */
	public String buildRedirectParam(History history, String anchor)
			throws UnsupportedEncodingException {
		return UriUtils.encodeQueryParam(history.getCurrent().toString()
				+ (anchor != null ? "#" + anchor : ""), "UTF-8");
	}

	/**
	 * Builds a redirection parameter from the given history.
	 *
	 * @see #buildRedirectParam(History, String)
	 */
	public String buildRedirectParam(History history)
			throws UnsupportedEncodingException {
		return this.buildRedirectParam(history, null);
	}

	/**
	 * Builds a redirect to the previous page.
	 */
	public String buildRedirectToPreviousPage(History history) {
		Optional<HistoryEntry> historyEntryOptional = history
				.getLastByRequestMethod(RequestMethod.GET, false);

		return historyEntryOptional.map(historyEntry -> "redirect:" + historyEntry)
				.orElse("redirect:/");
	}

	/**
	 * Builds a report creation URI for the given dossier.
	 */
	public UriBuilder buildReportCreationUri(Dossier dossier) {
		return this.builder(Entities.REPORT, Actions.CREATE).addEntityId(dossier);
	}

	/**
	 * Returns a {@link UriBuilder} for the given entity and action.
	 *
	 * <p>Convenience method for use in templates.</p>
	 */
	public UriBuilder builder(String entity, String action) {
		return UriBuilder.create(entity, action);
	}

	/**
	 * Returns a {@link UriBuilder} for absolute links (e.g. for mails).
	 */
	public UriBuilder builderAbsolute(String entity, String action) {
		return UriBuilder.create(entity, action, this.baseName);
	}
}
