/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.interfaces;

import java.util.Set;

import eu.nanodefine.etool.model.dto.CustomAttribute;

/**
 * Interface for entities with custom attributes.
 */
public interface ICustomAttributeEntity extends INamedEntity, IUserAwareEntity {

	/**
	 * Returns the set of custom attributes.
	 */
	public Set<CustomAttribute> getCustomAttributes();

	/**
	 * Sets the set of custom attributes.
	 */
	public void setCustomAttributes(Set<CustomAttribute> customAttributes);
}
