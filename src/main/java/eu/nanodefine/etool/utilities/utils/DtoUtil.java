/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import eu.nanodefine.etool.model.interfaces.IDataTransferObject;

/**
 * TODO documentation
 *
 * TODO move to service
 */
public class DtoUtil {

	public static boolean equals(Object o1, Object o2) {
		if (o1 instanceof IDataTransferObject &&
				o2 instanceof IDataTransferObject) {
			IDataTransferObject dto1 = (IDataTransferObject) o1;
			IDataTransferObject dto2 = (IDataTransferObject) o2;

			if (dto1 == dto2) {
				return true;
			} else if (dto1.getId() != null &&
					dto2.getId() != null) {
				return dto1.getId().equals(dto2.getId());
			}

		}

		return false;
	}
}
