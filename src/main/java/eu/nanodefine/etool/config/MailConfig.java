/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuration of the mailing subsystem.
 *
 * Source: http://www.thymeleaf.org/doc/articles/springmail.html
 */
@Configuration
public class MailConfig {

	@Value("${mail.server.host}")
	private String host;

	@Value("${mail.server.port}")
	private Integer port;

	@Value("${mail.server.protocol}")
	private String protocol;

	@Value("${mail.server.username}")
	private String username;

	@Value("${mail.server.password}")
	private String password;

	/**
	 * Configures the mail system and returns the {@link JavaMailSender} bean
	 * usable to send mails.
	 */
	@Bean
	public JavaMailSender mailSender() throws IOException {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(this.host);
		mailSender.setPort(this.port);
		mailSender.setProtocol(this.protocol);
		mailSender.setUsername(this.username);
		mailSender.setPassword(this.password);
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.smtp.auth", "true");
		javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
		javaMailProperties.setProperty("mail.smtp.timeout", "8500");
		// TODO allow additional java mail properties
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}
}
