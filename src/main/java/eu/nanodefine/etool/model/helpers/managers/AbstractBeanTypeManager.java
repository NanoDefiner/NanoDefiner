/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import eu.nanodefine.etool.exception.runtime.NoSuchBeanException;

/**
 * Manager of beans with a common interface.
 *
 * <p>Spring provides a way to collect beans implementing a certain interface or extending a
 * certain class. That principle is used here to provide access to groups of beans like services
 * and repositories, based on their class name or other identifier.</p>
 *
 * TODO change approach to only considering classes which conform to a specific
 * package name pattern (e.g. only classes within eu.nanodefine). This would
 * allow us to build the map initially, speeding things up (a bit).
 */
abstract public class AbstractBeanTypeManager<T>
		implements IBeanTypeManager<T> {

	/**
	 * Maps bean classes to actual beans.
	 */
	private Map<Class<? extends T>, T> beanMap = new HashMap<>();

	/**
	 * Looks for a compatible bean by walking through the map and testing assignability.
	 */
	private <E extends T> Optional<T> findCompatibleBean(Class<E> clazz) {

		T compatibleBean = null;

		// Try to identify compatible bean
		for (T bean : this.getAvailableBeans()) {
			if (clazz.isAssignableFrom(bean.getClass())) {
				compatibleBean = bean;
				break;
			}
		}

		return Optional.ofNullable(compatibleBean);
	}

	@Override
	abstract public List<T> getAvailableBeans();

	@Nonnull
	@Override
	public <S extends T> S getBean(Class<S> clazz) {
		// Try to add bean if it does not exist
		if (!this.beanMap.containsKey(clazz)) {
			Optional<T> bean = this.findCompatibleBean(clazz);

			// Add bean again, but using the requested class
			bean.ifPresent(t -> this.beanMap.put(clazz, t));
		}

		// Throw exception if it still does not exist
		if (!this.beanMap.containsKey(clazz)) {
			throw new NoSuchBeanException("No bean found for class: " + clazz.getCanonicalName());
		}

		return (S) this.beanMap.get(clazz);
	}

	@Override
	public Map<Class<? extends T>, T> getBeanMap() {
		return this.beanMap;
	}

	@Override
	public <S extends T> boolean hasBean(Class<S> clazz) {
		return this.beanMap.containsKey(clazz);
	}

}
