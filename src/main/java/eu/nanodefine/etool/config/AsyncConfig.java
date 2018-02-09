/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration of asynchronous processing.
 */
@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

	/**
	 * Provides an executor for
	 *
	 * {@link org.springframework.scheduling.annotation.Async}-annotated methods.
	 *
	 * TODO move configuration to property file
	 */
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("NanoDefinerAsync-");
		executor.initialize();
		return executor;
	}
}
