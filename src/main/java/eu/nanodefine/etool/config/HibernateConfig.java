/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.nanodefine.etool.controller.interceptors.HibernateStatisticsInterceptor;

/**
 * Hibernate configuration.
 */
@Configuration
public class HibernateConfig {

	private static Logger log = LoggerFactory.getLogger(HibernateConfig.class);

	/**
	 * Sets up the data source based on the hikari properties.
	 *
	 * This is responsible for the actual connection to the database.
	 */
	@Bean
	@Autowired
	public DataSource dataSource(ResourceLoader resourceLoader)
			throws IOException {
		log.debug("Creating Hikari data source");
		HikariConfig config = new HikariConfig(
				this.mergeProperties("file:config/hikari.properties.default",
						"file:config/hikari.properties", resourceLoader));
		HikariDataSource ds = new HikariDataSource(config);

		return ds;
	}

	/**
	 * Factory bean used to create the {@link javax.persistence.EntityManager}.
	 *
	 * Loads and merges the hibernate properties and adds the statistics
	 * interceptor. Uses the data source configured in
	 * {@link #dataSource(ResourceLoader)}.
	 *
	 * @see #hibernateInterceptor()
	 * @see #dataSource(ResourceLoader)
	 */
	@Bean
	@Autowired
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			ResourceLoader resourceLoader, DataSource dataSource) throws IOException {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("eu.nanodefine.etool.model.dto");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);

		Properties properties = this.mergeProperties(
				"file:config/hibernate.properties.default",
				"file:config/hibernate.properties", resourceLoader);

		properties.put("hibernate.ejb.interceptor", hibernateInterceptor());

		em.setJpaProperties(properties);

		return em;
	}

	/**
	 * Handles {@link javax.persistence.PersistenceException}s.
	 *
	 * @return
	 */
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	/**
	 * Interceptor used to create query statistics (printed to log).
	 *
	 * https://knes1.github.io/blog/2015/2015-07-08-counting-queries-per-request-with-hibernate-and-spring.html
	 */
	@Bean
	public HibernateStatisticsInterceptor hibernateInterceptor() {
		return new HibernateStatisticsInterceptor();
	}

	/**
	 * Merge property files.
	 *
	 * This function tries to read property files from two specified locations
	 * {@code defaultLocation} and {@code customLocation} and merges them if both
	 * files exist.
	 *
	 * Merging is done by first loading the {@code defaultLocation} property file,
	 * putting the loaded properties into a {@link Properties} instance and then
	 * loading the properties from the {@code customLocation} property file, also
	 * putting them in the {@link Properties} instance, overwriting existing
	 * values.
	 *
	 * @param defaultLocation Property files containing default values
	 * @param customLocation Property files containing custom values
	 * @param resourceLoader Resource loader used to load the property files
	 * @return Merged {@link Properties}
	 * @throws IOException
	 */
	private Properties mergeProperties(String defaultLocation,
			String customLocation, ResourceLoader resourceLoader) throws IOException {
		Resource resourceDefault = resourceLoader
				.getResource(defaultLocation);
		Resource resource = resourceLoader
				.getResource(customLocation);

		Properties properties = new Properties();

		// First load default, if the file exists
		if (resourceDefault.exists()) {
			properties.putAll(PropertiesLoaderUtils.loadProperties(resourceDefault));
		}

		// Then override with custom properties, if the file exists
		if (resource.exists()) {
			properties.putAll(PropertiesLoaderUtils.loadProperties(resource));
		}

		return properties;
	}

	/**
	 * Sets up the transaction manager, uses the entity manager factory from
	 * {@link #entityManagerFactory(ResourceLoader, DataSource)}.
	 */
	@Bean
	@Autowired
	public PlatformTransactionManager transactionManager(
			EntityManagerFactory emf, ResourceLoader resourceLoader, DataSource dataSource)
			throws IOException {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		transactionManager.setDataSource(dataSource);

		return transactionManager;
	}
}
