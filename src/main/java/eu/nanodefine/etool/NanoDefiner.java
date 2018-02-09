/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import eu.nanodefine.etool.config.MvcConfig;

/**
 * NanoDefiner main class.
 */
@SpringBootApplication
public class NanoDefiner extends AbstractAnnotationConfigDispatcherServletInitializer {

	/**
	 * Starts the {@link SpringApplication} for this class.
	 */
	public static void main(String[] args) {
		SpringApplication.run(NanoDefiner.class, args);
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { MvcConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return null;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	/**
	 * Post-startup hook.
	 *
	 * <p>Sets the default spring profile and adds the shiro filter proxy.</p>
	 */
	@Override
	public void onStartup(ServletContext container) throws ServletException {

		super.onStartup(container);

		this.logger.info("Starting NanoDefiner application");

		// Default profile
		// TODO extract to application.properties
		container.setInitParameter("spring.profiles.default", "dev");

		// Add shiro filter
		container.addFilter("shiroFilter", DelegatingFilterProxy.class)
				.addMappingForUrlPatterns(null, false, "/*");
	}
}
