/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;

/**
 * Configuration class for the Thymeleaf template engine.
 *
 * @see <a href="http://www.thymeleaf.org/">Thymeleaf website</a>
 */
@Configuration
public class ThymeleafConfig implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	// TODO why is spring ignoring this value?
	@Value("${spring.thymeleaf.cache}")
	private boolean cachable;

	private Logger logger = LoggerFactory.getLogger(ThymeleafConfig.class);

	/**
	 * Creates and configures the thymeleaf template resolver.
	 */
	@Bean
	public ITemplateResolver defaultTemplateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(this.applicationContext);
		resolver.setPrefix("/WEB-INF/templates/");
		resolver.setSuffix(".html");
		resolver.setCacheable(this.cachable);
		resolver.setTemplateMode(TemplateMode.HTML);
		return resolver;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Creates and configures the template engine itself using the template
	 * resolver.
	 *
	 * @see #defaultTemplateResolver()
	 */
	@Bean
	@Autowired
	public SpringTemplateEngine templateEngine(
			ITemplateResolver templateResolver) {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(templateResolver);
		// Enable shiro dialect for thymeleaf:
		// https://github.com/theborakompanioni/thymeleaf-extras-shiro
		engine.addDialect(new ShiroDialect());
		return engine;
	}

	/**
	 * Creates and configures the view resolver.
	 */
	@Bean
	@Autowired
	public ThymeleafViewResolver thymeleafViewResolver(
			SpringTemplateEngine templateEngine) {

		this.logger.debug("Creating Thymeleaf view resolver");

		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine);
		resolver.setApplicationContext(this.applicationContext);
		resolver.setCharacterEncoding("UTF-8");

		this.logger.debug("Finished Thymeleaf view resolver configuration");

		return resolver;
	}
}
