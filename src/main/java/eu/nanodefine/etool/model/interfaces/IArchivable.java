/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.interfaces;

import java.util.Date;

/**
 * Interface for achivable entities.
 */
public interface IArchivable {

	/**
	 * Returns whether the entity is archived.
	 */
	public boolean isArchived();

	/**
	 * Sets the archivation state of the entity.
	 */
	public void setArchived(boolean archived);

	/**
	 * Returns the change date of the entity.
	 */
	public Date getChangeDate();

	/**
	 * Sets the change date of entity.
	 */
	public void setChangeDate(Date date);
}
