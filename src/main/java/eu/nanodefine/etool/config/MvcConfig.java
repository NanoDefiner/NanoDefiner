/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.File;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import eu.nanodefine.etool.controller.interceptors.HistoryInterceptor;
import eu.nanodefine.etool.controller.interceptors.MessageInterceptor;
import eu.nanodefine.etool.controller.interceptors.RequestStatisticsInterceptor;

/**
 * General MVC configuration.
 *
 * Configures interceptors and beans.
 */
@Configuration
@EnableWebMvc
@ComponentScan({ "eu.nanodefine.etool" })
// Load application properties from default and custom files
@PropertySource(value = { "file:config/application.properties.default",
		"file:config/application.properties" }, ignoreResourceNotFound = true)
// Load build properties from default and custom files
@PropertySource(value = { "classpath:build.properties" }, ignoreResourceNotFound = true)
// Automatically provides and configures a ton of beans that are not added or
// configured manually
@EnableAutoConfiguration
// This allows annotations to be used to enable automatic transaction management
@EnableTransactionManagement(proxyTargetClass = true)
// Asynchronous processing of requests so that the user does not have to wait
// for tasks like PDF creation or mail sending to finish
@EnableAsync(proxyTargetClass = true)
// Enable JPA repositories. They are great.
@EnableJpaRepositories(basePackages = { "eu.nanodefine.etool.model.repositories" })
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Value("${server.data_directory}")
	private String analysisFileDirectory;

	private Logger log = LoggerFactory.getLogger(MvcConfig.class);

	@Autowired
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	@Value("${spring.jpa.properties.hibernate.jdbc.time_zone}")
	private String timeZone;

	/**
	 * Creates and adds interceptors to interceptor registry.
	 *
	 * TODO can interceptors be added automatically when annotated
	 * with @Component/@Bean?
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		this.log.debug("Adding interceptors");
		registry.addInterceptor(new MessageInterceptor());
		registry.addInterceptor(new HistoryInterceptor());
		registry.addInterceptor(requestStatisticsInterceptor());
		registry.addInterceptor(localeChangeInterceptor());

		super.addInterceptors(registry);
	}

	/**
	 * Adds recource mapping.
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**")
				.addResourceLocations("file:static/").setCachePeriod(0);
		registry.addResourceHandler("/**").addResourceLocations("/");
	}

	/**
	 * Disable default model on redirect.
	 *
	 * <p>When redirecting, spring appends existing model parameters to the URL by
	 * default. We don't want that, anything we want to pass along with a
	 * redirect goes into flash parameters; so we disable this behavior here.</p>
	 *
	 * <p>Also, we set the time zone.</p>
	 */
	@PostConstruct
	public void init() {
		this.requestMappingHandlerAdapter.setIgnoreDefaultModelOnRedirect(true);

		TimeZone.setDefault(TimeZone.getTimeZone(this.timeZone));

		// Validate analysis file directory
		try {
			this.log.info("Analysis file directory: {}", this.analysisFileDirectory);
			File dir = new File(this.analysisFileDirectory);

			if (!dir.exists()) {
				dir.mkdirs();
			}
		} catch (Exception e) {
			// TODO shouldn't we kind of kill the application at this point?
			throw new RuntimeException("Invalid analysis file directory: " +
					e.getMessage());
		}
	}

	private LocaleChangeInterceptor localeChangeInterceptor() {
		// This allows changing of the locale via request parameter, i.e. adding
		// ?lang=de_DE to the URL will change the language to german until it is
		// manually reset
		// TODO extract into method
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");

		return lci;
	}

	/**
	 * Create the {@link RequestStatisticsInterceptor} here since it needs
	 * auto-wiring.
	 *
	 * @see RequestStatisticsInterceptor
	 * @see eu.nanodefine.etool.controller.interceptors.HibernateStatisticsInterceptor
	 */
	@Bean
	public HandlerInterceptor requestStatisticsInterceptor() {
		return new RequestStatisticsInterceptor();
	}
}
