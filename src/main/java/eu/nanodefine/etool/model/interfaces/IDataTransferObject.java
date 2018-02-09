/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.interfaces;

/**
 * Simple interface to unify the handling of DTOs.
 */
public interface IDataTransferObject {

	/**
	 * Returns the entity type as defined in
	 * {@link eu.nanodefine.etool.constants.Entities}.
	 */
	public String getEntityType();

	/**
	 * Returns the DTOs ID.
	 */
	public Integer getId();

	/**
	 * Sets the DTOs ID.
	 */
	public void setId(Integer id);

}
