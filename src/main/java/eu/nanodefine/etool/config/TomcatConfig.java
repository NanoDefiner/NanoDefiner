/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import org.apache.catalina.Context;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat configuration.
 */
@Configuration
public class TomcatConfig {

	private Logger log = LoggerFactory.getLogger(TomcatConfig.class);

	/**
	 * Increases Tomcat's resource cache size.
	 *
	 * TODO move cache size to configuration
	 *
	 * TODO what if we don't use tomcat?
	 */
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		return new TomcatEmbeddedServletContainerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				final int cacheSize = 40 * 1024;
				StandardRoot standardRoot = new StandardRoot(context);
				standardRoot.setCacheMaxSize(cacheSize);
				context.setResources(standardRoot);

				TomcatConfig.this.log.info(String.format("New cache size (KB): %d",
						context.getResources().getCacheMaxSize()));
			}
		};
	}
}
