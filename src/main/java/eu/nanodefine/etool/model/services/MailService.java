/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.view.TranslationService;

/**
 * Service for creating and delivering mails.
 */
@Service
public class MailService implements ApplicationContextAware, IService {

	@Value("${mail.admins}")
	private String adminAddresses;

	private ApplicationContext applicationContext;

	private Logger log = LoggerFactory.getLogger(MailService.class);

	@Value("${mail.from}")
	private String mailFrom;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private TranslationService translationService;

	private Context getContext(Model model) {

		Map<String, Object> variables = model.asMap();
		variables.putIfAbsent("ctx", this.applicationContext);

		for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
			variables.putIfAbsent(beanName, this.applicationContext.getBean(beanName));
		}

		return new Context(this.translationService.getLocale(), variables);
	}

	/**
	 * Returns whether the mailing subsystem is enabled (i.e. properly
	 * configured).
	 *
	 * To disable the mail subsystem, set the configuration property
	 * {@literal mail.server.port} to a value below 0.
	 */
	public boolean isEnabled() {
		// Abort if invalid port
		// TODO solve cleaner
		return (((JavaMailSenderImpl) this.mailSender).getPort() >= 0);
	}

	@Async
	public void sendAdminMail(String template, String subject, Model model) {
		String[] recipients = this.adminAddresses.split(",");

		this.sendMail(template, recipients, subject, model);
	}

	/**
	 * Sends a mail using the given template and model variables.
	 */
	@Async
	public void sendMail(String template, String[] recipients, String subject,
			Model model) {

		// Abort if mail subsystem disabled
		if (!this.isEnabled()) {
			return;
		}

		// Make a copy of the model
		Model localModel = new ExtendedModelMap();
		localModel.addAllAttributes(new HashMap<>(model.asMap()));

		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
		try {
			message.setFrom(this.mailFrom);

			for (String to : recipients) {
				message.addTo(to);
			}

			message.setSubject(subject);

			message.setText(this.templateEngine.process(template,
					this.getContext(localModel)), true);
			this.mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			this.log.error("Unable to send mail", e);
		}

		this.log.info("Sent mail to: {}", (Object[]) recipients);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
