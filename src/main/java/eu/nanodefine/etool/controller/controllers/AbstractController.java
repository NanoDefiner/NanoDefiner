/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.controller.controllers;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.exception.runtime.InvalidRequestParametersException;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.model.dto.Dossier;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.RepositoryManager;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.model.interfaces.IUserAwareEntity;
import eu.nanodefine.etool.model.services.technique.TechniqueService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.model.services.view.UriService;
import eu.nanodefine.etool.security.NanoDefinerRealm;
import eu.nanodefine.etool.view.helper.action.ActionListEntry;

/**
 * Abstract controller.
 *
 * <p>Provides important beans like the {@link RepositoryManager} and {@link ServiceManager} as
 * well as common functionality for all controllers.</p>
 */
public abstract class AbstractController {

	@Autowired
	protected LocaleResolver localeResolver;

	/**
	 * SLF4J logger.
	 */
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * {@link PerformanceConfiguration} bean.
	 */
	@Autowired
	protected PerformanceConfiguration performanceConfiguration;

	/**
	 * {@link PerformanceDictionary} bean.
	 */
	@Autowired
	protected PerformanceDictionary performanceDictionary;

	/**
	 * Manager for repository beans, for convenient access.
	 */
	@Autowired
	protected RepositoryManager repositoryManager;

	/**
	 * Shiro {@link Realm} of the application.
	 */
	@Autowired
	protected NanoDefinerRealm securityRealm;

	@Resource(name = "techniques")
	protected List<Technique> techniques;

	/**
	 * Provides access to the {@link TranslationService} for all controllers.
	 */
	@Autowired
	protected TranslationService translationService;

	/**
	 * Provides access to a {@link UriService} instance for all controllers.
	 */
	@Autowired
	protected UriService uriService;

	/**
	 * Manager for service beans, for convenient access.
	 */
	@Autowired
	protected ServiceManager serviceManager;

	/**
	 * Create action list for custom attributes.
	 *
	 * <p>Custom attributes are available for dossiers, methods and materials, so the action list
	 * is available in the abstract controller.</p>
	 */
	protected List<ActionListEntry> customAttributeActionList(ICustomAttributeEntity entity) {
		String path = this.uriService.builder(Entities.CUSTOM_ATTRIBUTE, Actions.CREATE)
				.addEntityIdQuery(entity).build();

		return ImmutableList.of(new ActionListEntry(path, "custom_attribute.create.action"));
	}

	/**
	 * Convenience method for filtering techniques.
	 *
	 * TODO move somewhere else?
	 */
	protected List<Technique> filterTechniquesForDossier(Dossier dossier) {
		return this.serviceManager.getBean(TechniqueService.class)
				.filterTechniquesForDossier(this.techniques, dossier);
	}

	/**
	 * Returns the current {@link User} from the Shiro session.
	 */
	@ModelAttribute("userSession")
	public User getCurrentUser() {
		return this.securityRealm.getUser();
	}

	/**
	 * Retrieve a value from the shiro {@link org.apache.shiro.session.Session}.
	 *
	 * Uses the method
	 * {@link org.apache.shiro.session.Session#getAttribute(Object)} internally.
	 *
	 * TODO move to service layer?
	 */
	public Object getSessionAttribute(Object key) {
		return this.getSessionAttribute(key, null);
	}

	/**
	 * Retrieve a value from the shiro {@link org.apache.shiro.session.Session},
	 * returning a default value if the session or sesstion attribute does not
	 * exist.
	 */
	public Object getSessionAttribute(Object key, Object defaultValue) {
		if (!this.securityRealm.hasSession()) {
			return defaultValue;
		}

		Object value = this.securityRealm.getSession().getAttribute(key);

		return value != null ? value : defaultValue;
	}

	/**
	 * Convenience method to add form errors to the global error list.
	 */
	protected void registerErrors(BindingResult result, List<String> errors) {
		for (FieldError error : result.getFieldErrors()) {
			errors.add(error.getCode());
		}
	}

	@ModelAttribute
	public void updateLocale(HttpServletRequest request, HttpServletResponse response) {
		if (this.getCurrentUser().getLocale() != null) {
			Locale locale =
					this.translationService.getLocaleFromString(this.getCurrentUser().getLocale());
			this.localeResolver.setLocale(request, response, locale);
			LocaleContextHolder.setLocale(locale);
		}
	}

	/**
	 * Entity validation.
	 *
	 * @see #validateUserAwareEntities(String, IUserAwareEntity...)
	 */
	public void validateUserAwareEntities(IUserAwareEntity... entities) {
		this.validateUserAwareEntities("Trying to access entity or page which does not belong to "
				+ "this user", entities);
	}

	/**
	 * Entity validation.
	 *
	 * <p>This method is used to ensure that a user can only access entities that belong to
	 * them.</p>
	 *
	 * TODO how do we deal with @ResponseBody methods?
	 */
	public void validateUserAwareEntities(String message, IUserAwareEntity... entities) {
		for (IUserAwareEntity entity : entities) {
			if (!this.validateUserAwareEntity(entity)) {
				throw new InvalidRequestParametersException(message);
			}
		}
	}

	/**
	 * Entity validation.
	 *
	 * <p>Validates a single user-aware entity. An entity passes validation if it is associated with
	 * the current user.</p>
	 *
	 * TODO add exception for admin users?
	 */
	public boolean validateUserAwareEntity(IUserAwareEntity entity) {
		return (entity instanceof IDataTransferObject)
				&& (entity.getUser() != null && entity.getUser().equals(this.getCurrentUser()));
	}

}
