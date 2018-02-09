/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.managers;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Interface for bean type managers.
 */
public interface IBeanTypeManager<T> {

	/**
	 * Returns a list of available beans.
	 */
	List<T> getAvailableBeans();

	/**
	 * Returns a bean compatible to the given class.
	 *
	 * <p>If no such bean was found, an exception is thrown.</p>
	 */
	@Nonnull
	<S extends T> S getBean(Class<S> clazz);

	/**
	 * Returns whether the
	 */
	<S extends T> boolean hasBean(Class<S> clazz);

	/**
	 * Returns the map of managed beans.
	 */
	Map<Class<? extends T>, T> getBeanMap();
}
