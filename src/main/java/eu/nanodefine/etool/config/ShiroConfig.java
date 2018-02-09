/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import eu.nanodefine.etool.security.NanoDefinerRealm;

/**
 * Shiro configuration.
 */
@Configuration
public class ShiroConfig {

	private Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

	/**
	 * Provides the {@link AuthorizationAttributeSourceAdvisor} bean.
	 *
	 * TODO needed?
	 *
	 * @see <a href="https://shiro.apache.org/spring.html">Shiro
	 * documentation</a>
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
			SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
		advisor.setSecurityManager(securityManager);

		return advisor;
	}

	/**
	 * Provides the {@link DefaultAdvisorAutoProxyCreator} bean.
	 *
	 * @see <a href="https://shiro.apache.org/spring.html">Shiro
	 * documentation</a>
	 * TODO needed?
	 */
	@Bean
	@DependsOn("lifeCycleBeanPostProcessor")
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator pc = new DefaultAdvisorAutoProxyCreator();

		pc.setProxyTargetClass(true);

		return pc;
	}

	/**
	 * Provides the {@link LifecycleBeanPostProcessor} bean.
	 *
	 * @see <a href="https://shiro.apache.org/spring.html">Shiro
	 * documentation</a>
	 * TODO needed?
	 */
	@Bean
	public LifecycleBeanPostProcessor lifeCycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * Creates and configures the {@link SecurityManager}.
	 */
	@Bean
	@Autowired
	public SecurityManager securityManager(NanoDefinerRealm realm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(
				realm);
		CookieRememberMeManager rememberMeManager = ((CookieRememberMeManager) securityManager
				.getRememberMeManager());
		rememberMeManager.getCookie().setName("rememberme.etool.nanodefine.eu");
		rememberMeManager.getCookie().setMaxAge(Cookie.ONE_YEAR);

		SecurityUtils.setSecurityManager(securityManager);

		this.logger.debug("SecurityManager configured and set");

		return securityManager;
	}

	/**
	 * Creates and configures the shiro filter.
	 */
	@Bean
	public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
		ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();

		filter.setSecurityManager(securityManager);
		// TODO when are these used?
		filter.setLoginUrl("/user/login");
		filter.setSuccessUrl("/");
		filter.setUnauthorizedUrl("/error/unauthorized");

		return filter;
	}
}
