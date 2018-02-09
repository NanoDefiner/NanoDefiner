/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.predicates.dossier;

import com.google.common.base.Predicate;

import eu.nanodefine.etool.model.dto.Dossier;

/**
 * Interface for dossier-aware predicates.
 */
public interface IDossierAwarePredicate<T> extends Predicate<T> {

	Dossier getDossier();

	void setDossier(Dossier dossier);
}
