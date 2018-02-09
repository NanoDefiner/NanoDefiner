/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.predicates.dossier;

import eu.nanodefine.etool.model.dto.Dossier;

/**
 * Abstract predicate class giving access to a dossier.
 */
public abstract class AbstractDossierAwarePredicate<T>
		implements IDossierAwarePredicate<T> {

	private Dossier dossier;

	@Override
	public Dossier getDossier() {
		return this.dossier;
	}

	@Override
	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}
}
