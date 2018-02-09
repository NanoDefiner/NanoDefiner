/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.exception.runtime;

/**
 * Exception for critical errors occuring during the configuration phase of the application start.
 */
public class ConfigurationException extends RuntimeException {

	public ConfigurationException(String message) {
		super(message);
	}

}
