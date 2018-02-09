/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.managers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Manages {@link CrudRepository} instances for easier access from the controllers.
 *
 * The method {@link #getBean(Class)} returns the repository object for the given
 * repository {@link Class} or <code>null</code> if the service does not exist.
 *
 * Available repositories include all classes implementing the {@link CrudRepository}
 * interface.
 */
@Component
public class RepositoryManager
		extends AbstractBeanTypeManager<CrudRepository<?, ?>> {

	@Autowired
	private List<CrudRepository<?, ?>> repositories;

	@Override
	public List<CrudRepository<?, ?>> getAvailableBeans() {
		return this.repositories;
	}
}
