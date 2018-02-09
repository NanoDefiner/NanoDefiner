/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.security;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.repositories.UserRepository;

/**
 * Realm implementation using a database backend.
 */
@Component
public class NanoDefinerRealm extends AuthorizingRealm {

	/**
	 * User repository used to load user account data.
	 */
	private final UserRepository userRepository;

	/**
	 * Realm initialization state.
	 *
	 * At the moment, this is not really used, but only indicates whether the realm has been properly
	 * created and initialized as a bean and {@link #initialize()} has been called.
	 */
	private Boolean initialized = Boolean.FALSE;

	private Logger log = LoggerFactory.getLogger(NanoDefinerRealm.class);

	@Autowired
	public NanoDefinerRealm(UserRepository userRepository) {
		this.log.info("realm has been created");
		this.userRepository = userRepository;
	}

	/**
	 * Creates a {@link SimpleAuthenticationInfo} object from the username and password hash.
	 *
	 * @throws AuthenticationException if an invalid username was provided or the user was not found
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;

		// Check principal
		String principal = upToken.getUsername();
		if (principal == null || principal.isEmpty()) {
			throw new AccountException("null or empty usernames are not allowed");
		}

		// Look up subject
		Optional<User> user = this.userRepository.findFirstByUsername(principal);
		if (!user.isPresent()) {
			throw new UnknownAccountException("no account for user: " + principal);
		}

		// Process authentication
		return new SimpleAuthenticationInfo(principal,
				user.get().getPasswordHash().toCharArray(), this.getName());
	}

	/**
	 * Creates a {@link SimpleAuthorizationInfo} object using the user roles.
	 *
	 * @see #findRoles(String)
	 * @see User#getRoles()
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
		return new SimpleAuthorizationInfo(
				this.findRoles((String) pc.getPrimaryPrincipal()));
	}

	/**
	 * Loads the user with the given username and returns a set of its roles or an empty set if the
	 * user could not be loaded.
	 */
	private Set<String> findRoles(String username) {
		Optional<User> userOptional = this.userRepository
				.findFirstByUsername(username);

		return userOptional.map(User::getRoles).orElseGet(ImmutableSet::of);
	}

	/**
	 * Returns the shiro {@link Session} for the current user.
	 *
	 * If no user exists in the session but the user is remembered according to shiro, the user is
	 * loaded from database.
	 *
	 * @throws UnknownAccountException if the user could not be loaded
	 */
	public Session getSession() throws UnknownAccountException {
		Subject subject = this.getShiroSubject();
		// Insert user object into session for remembered users
		if (subject.isRemembered() && subject.getPrincipal() != null
				&& subject.getSession().getAttribute(Entities.USER) == null) {

			// TODO handle exception better
			User user = this.userRepository
					.findFirstByUsername(subject.getPrincipal().toString())
					.orElseThrow(UnknownAccountException::new);

			subject.getSession().setAttribute(Entities.USER, user);
		}

		return subject.getSession(false);
	}

	/**
	 * Returns the Shiro {@link Subject} of the current thread.
	 *
	 * Convenience method for quicker access and encapsulation.
	 */
	public Subject getShiroSubject() {
		return SecurityUtils.getSubject();
	}

	/**
	 * Returns the logged-in user or an empty {@link User} instance if the user
	 * is not logged in.
	 */
	public User getUser() {
		User user = null;

		if (this.hasSession()) {
			user = (User) this.getSession().getAttribute(Entities.USER);
		}

		return user != null ? user : new User();
	}

	/**
	 * Returns whether a shiro session is associated with this thread's shiro
	 * {@link Subject}.
	 */
	public boolean hasSession() {
		return this.getSession() != null;
	}

	/**
	 * Initializes the realm.
	 *
	 * TODO extract admin user creation
	 */
	@PostConstruct
	public void initialize() {
		if (!this.initialized) {

			// Check if users exist and create default admin account if not
			if (!this.userRepository.findAll().iterator().hasNext()) {
				Date now = new Date();
				// Add some configurable salt?
				User admin = new User("admin", "admin", "none", "salt", now,
						"admin", "admin", "admin@localhost", false,
						new Locale("en", "GB").toString());
				admin.setActivationDate(now);
				admin.setAccess("admin");
				this.userRepository.save(admin);
				this.log.info("default admin user created");
			}

			this.log.info("realm has been initialized");
			this.initialized = true;
		}
	}

	/**
	 * Returns the realm's initialization state.
	 *
	 * @return <code>true</code> if initialized, <code>false</code> if not
	 */
	public Boolean isInitialized() {
		return this.initialized;
	}
}
