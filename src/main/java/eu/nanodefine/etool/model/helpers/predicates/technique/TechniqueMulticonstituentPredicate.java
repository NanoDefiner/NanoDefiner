/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.predicates.technique;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.configurations.beans.PerformanceCriterion;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.predicates.dossier.AbstractDossierAwarePredicate;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Filter technique for multiconstituent support.
 */
@Component
public class TechniqueMulticonstituentPredicate extends
		AbstractDossierAwarePredicate<Technique> {

	private final PerformanceConfiguration performanceConfiguration;

	@Autowired
	public TechniqueMulticonstituentPredicate(
			PerformanceConfiguration performanceConfiguration) {
		this.performanceConfiguration = performanceConfiguration;
	}

	@Override
	public boolean apply(Technique input) {

		ConfiguredPerformance performance =
				this.performanceConfiguration.getEntry(input.getSignifier());

		PerformanceCriterion criterion = performance
				.getEntry(PerformanceAttributes.MULTICONSTITUENT);
		Boolean techniqueSupportsMulticonstituent = ConfigurationUtil
				.toBinary(criterion.getValue());

		// TODO unknown is treated the same way as 'yes', is this correct?
		if (techniqueSupportsMulticonstituent == null) {
			techniqueSupportsMulticonstituent = Boolean.TRUE;
		}

		return !(this.getDossier().isMulticonstituent()
				&& techniqueSupportsMulticonstituent.equals(Boolean.FALSE));
	}
}
