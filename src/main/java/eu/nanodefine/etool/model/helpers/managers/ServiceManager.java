/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.managers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Manages {@link IService} instances for easier access from the controllers.
 *
 * The method {@link #getBean(Class)} returns the service object for the given
 * service {@link Class} or <code>null</code> if the service does not exist.
 *
 * Available services include all classes implementing the {@link IService}
 * interface that are annotated with
 * {@link org.springframework.stereotype.Service}.
 */
@Component
public class ServiceManager extends AbstractBeanTypeManager<IService> {

	@Autowired
	private List<IService> services;

	@Override
	public List<IService> getAvailableBeans() {
		return this.services;
	}
}
