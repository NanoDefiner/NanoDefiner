/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Log request statistics (query count and request time) to console.
 *
 * https://knes1.github.io/blog/2015/2015-07-08-counting-queries-per-request-with-hibernate-and-spring.html
 */
@Component
public class RequestStatisticsInterceptor extends HandlerInterceptorAdapter {

	private final Logger log = LoggerFactory
			.getLogger(RequestStatisticsInterceptor.class);

	@Autowired
	private HibernateStatisticsInterceptor statisticsInterceptor;

	private ThreadLocal<Long> time = new ThreadLocal<>();

	/**
	 * After completing the request, statistics are printed to console.
	 */
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		long duration = System.currentTimeMillis() - this.time.get();
		Long queryCount = this.statisticsInterceptor.getQueryCount();
		this.statisticsInterceptor.clearCounter();
		this.time.remove();
		this.log.info("[Time: {} ms] [Queries: {}] {} {}", duration, queryCount,
				request.getMethod(), request.getRequestURI());
	}

	/**
	 * Concurrent handling is not supported.
	 */
	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		//concurrent handling cannot be supported here
		this.statisticsInterceptor.clearCounter();
		this.time.remove();
	}

	/**
	 * Before handling the request, initialize timer and query counter.
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		this.time.set(System.currentTimeMillis());
		this.statisticsInterceptor.startCounter();
		return true;
	}
}
