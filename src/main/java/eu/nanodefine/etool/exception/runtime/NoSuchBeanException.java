/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.exception.runtime;

/**
 * Thrown by bean type managers when a bean was not found.
 */
public class NoSuchBeanException extends RuntimeException {

	public NoSuchBeanException(String message) {
		super(message);
	}

}
