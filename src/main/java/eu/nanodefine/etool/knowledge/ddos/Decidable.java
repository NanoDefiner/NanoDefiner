/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.knowledge.ddos;

import eu.nanodefine.etool.knowledge.configurations.beans.PerformanceCriterion;
import eu.nanodefine.etool.model.dto.MaterialCriterion;

/**
 * This was thought to be useful for criteria objects like
 * {@link PerformanceCriterion} and {@link MaterialCriterion} but may get
 * irrelevant for creating more complexity than needed.
 *
 * TODO Simple refactoring for elimination of this interface.
 *
 * Refactoring may be applied in the DDO classes.
 *
 */
public interface Decidable {

	String getName();

	String getValue();

}
