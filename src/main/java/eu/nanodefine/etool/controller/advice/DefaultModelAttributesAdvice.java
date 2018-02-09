/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.advice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.controller.helper.history.History;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.utilities.classes.UriBuilder;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Controller advice for globally available model attributes.
 *
 * Currently, these attributes are:
 *
 * <ul>
 * <li><em>actionList:</em> Actions to be displayed in the action box. This
 * box is automatically shown or hidden depending on whether actions exist
 * </li>
 * <li><em>breadcrumbs:</em> Breadcrumbs displaying the position within the
 * application at the top of the page</li>
 * </ul>
 */
@ControllerAdvice
public class DefaultModelAttributesAdvice {

	/**
	 * Empty list of {@link ActionListEntry} objects, to be populated in the
	 * controller.
	 */
	@ModelAttribute("actionList")
	public List<ActionListEntry> actionList() {
		return new ArrayList<>();
	}

	/**
	 * Automatically creates the breadcrumbs entries for the current page.
	 *
	 * Each breadcrumb entry consists of one or three parts:
	 *
	 * <ul>
	 * <li><em>ID:</em> Entity associated with this breadcrumb, used for the
	 * <i>data-classes</i> HTML attribute</li>
	 * <li><em>URI:</em> A link associated with the breadcrumb, usually the
	 * {@link Actions#READ} action for the given entity. If not provided, the
	 * breadcrumb entry will be displayed without a link</li>
	 * <li><em>Label:</em> Label of the breadcrumb, usually the name of the
	 * given entity. If this is not provided, it indicates a new entity is being
	 * creates and "New" is initially displayed and updated with the user-given
	 * entity name on the client-side via JavaScript</li>
	 * </ul>
	 */
	@ModelAttribute("breadcrumbs")
	public List<String[]> breadcrumbs(@ModelAttribute Dossier dossier,
			@ModelAttribute Material material, @ModelAttribute Method method,
			@ModelAttribute Report report,
			@RequestAttribute History history) {

		List<String[]> breadcrumbs = new ArrayList<>();

		// Use entity instead of / in addition to the dossier ID check to add
		// a non-linked entry for entity creation processes

		// Add dossier breadcrumb
		if (dossier.getId() != 0) {
			String dossierAnchor = history.getCurrent().getEntity();

			// Add "s" for tabs "materials", "methods", or "reports"
			if (!history.getCurrent().getEntity().equals(Entities.DOSSIER)) {
				dossierAnchor += "s";
			}

			breadcrumbs.add(new String[] {
					Entities.DOSSIER, UriBuilder.create(Entities.DOSSIER, Actions.READ)
					.addEntityId(dossier).setAnchor(dossierAnchor).build(),
					dossier.getName()
			});
		}

		// Add breadcrumb for the active entity – only one of the following can be
		// true
		if (material.getId() != 0) {
			breadcrumbs.add(new String[] {
					Entities.MATERIAL, UriBuilder.create(Entities.MATERIAL, Actions.READ)
					.addEntityId(material).build(),
					material.getName()
			});
		} else if (method.getId() != 0) {
			breadcrumbs.add(new String[] {
					Entities.METHOD, UriBuilder.create(Entities.METHOD, Actions.READ)
					.addEntityId(method).build(),
					method.getName()
			});
		} else if (report.getId() != 0) {
			breadcrumbs.add(new String[] {
					Entities.REPORT, UriBuilder.create(Entities.REPORT, Actions.READ)
					.addEntityId(report).build(),
					report.getName()
			});
		} else if (Objects.equals(history.getCurrent().getAction(),
				Actions.CREATE)) {
			breadcrumbs.add(new String[] { history.getCurrent().getEntity()
			});
		}

		return breadcrumbs;
	}

}
