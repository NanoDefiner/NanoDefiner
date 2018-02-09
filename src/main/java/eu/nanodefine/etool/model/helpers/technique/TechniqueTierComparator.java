/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.technique;

import java.util.Collections;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.services.technique.TechniqueService;

/**
 * Compares techniques using their tiers.
 *
 * @see TechniqueService#getTiers(Technique)
 */
@Component
public class TechniqueTierComparator implements Comparator<Technique> {

	private TechniqueService techniqueService;

	@Autowired
	public TechniqueTierComparator(
			TechniqueService techniqueService) {

		this.techniqueService = techniqueService;
	}

	@Override
	public int compare(Technique t1, Technique t2) {

		Integer tier1 = Collections.min(this.techniqueService.convertTiersToInt(
				this.techniqueService.getTiers(t1)));

		Integer tier2 = Collections.min(this.techniqueService.convertTiersToInt(
				this.techniqueService.getTiers(t2)));

		return tier1.compareTo(tier2);
	}

	@Override
	public Comparator<Technique> reversed() {
		return Collections.reverseOrder(this);
	}
}
