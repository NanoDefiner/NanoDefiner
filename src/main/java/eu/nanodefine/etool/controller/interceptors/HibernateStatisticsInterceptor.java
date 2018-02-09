/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.interceptors;

import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that counts SQL queries.
 *
 * @see RequestStatisticsInterceptor
 *
 * TODO try to reduce number of queries, log full queries
 *
 * https://knes1.github.io/blog/2015/2015-07-08-counting-queries-per-request-with-hibernate-and-spring.html
 */
public class HibernateStatisticsInterceptor extends EmptyInterceptor {

	private Logger log = LoggerFactory
			.getLogger(HibernateStatisticsInterceptor.class);

	private ThreadLocal<Long> queryCount = new ThreadLocal<>();

	public void clearCounter() {
		this.queryCount.remove();
	}

	public Long getQueryCount() {
		return this.queryCount.get();
	}

	@Override
	public String onPrepareStatement(String sql) {
		Long count = this.queryCount.get();
		if (count != null) {
			this.queryCount.set(count + 1);
		}
		//log.trace(sql);
		return super.onPrepareStatement(sql);
	}

	public void startCounter() {
		this.queryCount.set(0L);
	}
}
