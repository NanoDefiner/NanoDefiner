/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.interfaces;

import eu.nanodefine.etool.model.dto.User;

/**
 * Interface for user-aware entities.
 */
public interface IUserAwareEntity {

	/**
	 * Returns the entity-related user.
	 */
	public User getUser();

	/**
	 * Sets the entity-related user.
	 */
	public void setUser(User user);
}
