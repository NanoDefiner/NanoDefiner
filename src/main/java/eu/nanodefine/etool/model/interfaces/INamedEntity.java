/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.interfaces;

/**
 * Interface for entities which have name.
 */
public interface INamedEntity extends IDataTransferObject {

	/**
	 * Returns the entity name.
	 */
	public String getName();

	/**
	 * Sets the entity name.
	 */
	public void setName(String name);
}
