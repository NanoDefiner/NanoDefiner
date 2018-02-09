/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.handler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

/**
 * Handler for exceptions during asynchronous processing.
 */
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	private Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

	/**
	 * Handles any exceptions during asynchronous processing by logging them.
	 *
	 * TODO send admin mail or create issue or something?
	 */
	@Override
	public void handleUncaughtException(Throwable ex, Method method,
			Object... params) {
		this.log.error("Error during asynchronous processing:", ex);
	}
}
