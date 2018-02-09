/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.exception.runtime;

/**
 * Exception for invalid request parameters.
 *
 * <p>Invalid request parameters can include cases like:</p>
 *
 * <ul>
 * <li>Access to entities that don't belong to the user</li>
 * <li>Access to non-existing entities</li>
 * <li>Invalid parameter values</li>
 * </ul>
 */
public class InvalidRequestParametersException extends RuntimeException {

	public InvalidRequestParametersException(String message) {
		super(message);
	}
}
