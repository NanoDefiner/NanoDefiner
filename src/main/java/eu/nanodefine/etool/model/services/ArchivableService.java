/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.nanodefine.etool.model.interfaces.IArchivable;
import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for entity archival.
 */
@Service
public class ArchivableService implements IService {

	/**
	 * Archives the given entities.
	 */
	@Transactional
	public void archiveEntities(Iterable<? extends IArchivable> entities) {
		entities.forEach(e -> e.setArchived(true));
	}

	/**
	 * Extract non-archived entity.
	 *
	 * <p>Returns the first entity which is not archived or {@literal null} if all
	 * given entities are archived.</p>
	 *
	 * TODO when this is used in the context of dossier materials, it indicates
	 * lack of support for multiconstituent samples!
	 */
	public <T extends IArchivable> T getFirstNotArchived(Iterable<T> entities) {
		for (T entity : entities) {
			if (!entity.isArchived()) {
				return entity;
			}
		}

		return null;
	}
}
