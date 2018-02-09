/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.predicates.technique;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.predicates.dossier.AbstractDossierAwarePredicate;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Filter technique for purpose.
 */
@Component
public class TechniquePurposePredicate
		extends AbstractDossierAwarePredicate<Technique> {

	@Autowired
	private PerformanceConfiguration performanceConfiguration;

	@Override
	public boolean apply(Technique input) {

		assert this.getDossier() != null;

		// Special case: null purpose, used when filtering for multiconstituent only
		if (this.getDossier().getPurpose() == null) {
			return true;
		}

		Set<String> dossierPurpose = ConfigurationUtil
				.toSet(this.getDossier().getPurpose());
		Set<String> techniquePurposes = ConfigurationUtil
				.toSet(this.performanceConfiguration.getEntry(input.getSignifier())
						.getEntry(PerformanceAttributes.PURPOSE).getValue());
		techniquePurposes.retainAll(dossierPurpose);

		return !techniquePurposes.isEmpty();
	}

	@Override
	public boolean equals(Object object) {
		return object.getClass().equals(TechniquePurposePredicate.class);
	}
}
