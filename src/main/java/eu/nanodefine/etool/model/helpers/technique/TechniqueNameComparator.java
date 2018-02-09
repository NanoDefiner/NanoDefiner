/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.technique;

import java.util.Collections;
import java.util.Comparator;

import eu.nanodefine.etool.model.dto.Technique;

/**
 * Compares techniques using their names.
 */
public class TechniqueNameComparator implements Comparator<Technique> {

	@Override
	public int compare(Technique o1, Technique o2) {
		return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
	}

	@Override
	public Comparator<Technique> reversed() {
		return Collections.reverseOrder(this);
	}
}
