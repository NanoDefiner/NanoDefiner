/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers.dossier;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.controller.controllers.AbstractController;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Option;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.repositories.DossierRepository;
import eu.nanodefine.etool.model.services.dossier.DossierService;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Abstract controller for dossiers.
 */
public abstract class AbstractDossierController extends AbstractController {

	/**
	 * Provides access to the {@link DossierRepository} for dossier controllers.
	 */
	@Autowired
	protected DossierRepository dossierRepository;

	/**
	 * Provides access to the {@link DossierService} for dossier controllers.
	 */
	@Autowired
	protected DossierService dossierService;

	/**
	 * Creates a list of dossier actions.
	 *
	 * <p>Creates a list of dossier-specific actions, more specifically:</p>
	 *
	 * <ul>
	 * <li>Update dossier</li>
	 * <li>Archive dossier</li>
	 * </ul>
	 *
	 * TODO move to service
	 */
	protected List<ActionListEntry> dossierActionList(Dossier dossier,
			History history) {
		String update = this.uriService.builder(Entities.DOSSIER, Actions.UPDATE)
				.addEntityId(dossier).build();

		String archiveRedirect =
				this.uriService.builder(Entities.DOSSIER, Actions.LIST).setAnchor("archived").build();

		String archive = this.uriService
				.builder(Entities.DOSSIER, Actions.ARCHIVE).addEntityId(dossier)
				.addRedirectParam(archiveRedirect).build();

		return ImmutableList.of(new ActionListEntry(update, "dossier.read.dossier_update", "update"),
				new ActionListEntry(archive, "dossier.read.dossier_archive", "archive"));
	}

	/**
	 * Exposes the dossier for binding.
	 *
	 * Exposes {@link eu.nanodefine.etool.controller.advice.RequestIdsAdvice#dossier(Optional,
	 * Optional, Material, Method, Report)}, overwriting {@code binding = false}.
	 */
	@ModelAttribute("dossierForm")
	public Dossier dossierForm(@ModelAttribute Dossier dossier) {
		return dossier;
	}

	/**
	 * Adds dossier purposes to the model.
	 */
	@ModelAttribute("dossierPurposes")
	protected List<Option> dossierPurposes() {

		return this.performanceDictionary
				.getAttribute(PerformanceAttributes.PURPOSE).getOptions();
	}

	/**
	 * Adds dossier sample types to the model.
	 *
	 * <p>These are just {@code boolean}s containig whether it is a mono/multi-constituent
	 * sample.</p>
	 *
	 * TODO solve this better
	 */
	@ModelAttribute("dossierSampleTypes")
	private boolean[] dossierSampleTypes() {
		return new boolean[] { false, true };
	}

	/**
	 * Make techniques available for preview.
	 */
	@ModelAttribute("techniques")
	public List<Technique> techniques() {
		return this.techniques;
	}
}
