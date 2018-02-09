/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

import eu.nanodefine.etool.constants.Actions;
import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.ProfileRepository;
import eu.nanodefine.etool.model.repositories.UserRepository;
import eu.nanodefine.etool.model.services.MailService;
import eu.nanodefine.etool.model.services.view.TranslationService;
import eu.nanodefine.etool.model.services.view.UriService;
import eu.nanodefine.etool.security.NanoDefinerRealm;
import eu.nanodefine.etool.view.form.UserForm;

/**
 * Service for user-related processing.
 */
@Service
public class UserService implements IService {

	private Logger log = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private ProfileRepository profileRepository;

	/**
	 * The encryption algorithm to be used for user passwords.
	 *
	 * @see MessageDigest#getInstance(String)
	 */
	@Value("${security.jbcrypt.saltrounds}")
	private Integer saltrounds;

	@Autowired
	private NanoDefinerRealm securityRealm;

	@Autowired
	private ServiceManager serviceManager;

	@Autowired
	private TranslationService ts;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Activates the given user.
	 *
	 * <p>Activating a user involves setting the activation code to {@code null} and updating the
	 * activation date.</p>
	 */
	@Transactional
	public void activateUser(User user) {
		user.setActivationCode(null);
		user.setActivationDate(new Date());

		this.userRepository.save(user);
	}

	/**
	 * Activates the given list of users.
	 *
	 * @see #activateUser(User)
	 */
	@Transactional
	public void activateUsers(List<User> users) {
		users.forEach(this::activateUser);
	}

	/**
	 * Adds the given role to the given user.
	 */
	@Transactional
	public User addRole(User user, String role) {
		user.addRole(role);

		return this.userRepository.save(user);
	}

	/**
	 * Creates and activation link for the given user.
	 *
	 * <p>Used in the template mail/user_activate.</p>
	 *
	 * TODO move to template service
	 */
	public String buildActivationLink(User user) {
		assert user.getActivationCode() != null;

		UriService uriService = this.serviceManager.getBean(UriService.class);

		return uriService.builderAbsolute(Entities.USER, Actions.CREATE).addPathParam(Actions.ACTIVATE)
				.addEntityId(user).addPathParam("code", user.getActivationCode()).build();
	}

	/**
	 * Creates and authentication token for the given user and password.
	 */
	public UsernamePasswordToken buildAuthenticationToken(User user,
			String password) {

		String passwordHashed;

		switch (user.getHashFunction()) {
			case "bcrypt":
				passwordHashed = this.buildBCryptHash(user, password);
				break;
			default:
				passwordHashed = this.buildDigestHash(user, password);
		}

		// Create token
		return new UsernamePasswordToken(user.getUsername(), passwordHashed);
	}

	/**
	 * Creates a BCrypt hash for the given user and password.
	 *
	 * <p>Returns an empty string if the password does not match or the user password hash otherwise.
	 * </p>
	 */
	private String buildBCryptHash(User user, String password) {

		if (BCrypt.checkpw(password, user.getPasswordHash())) {
			return user.getPasswordHash();
		}

		return "";
	}

	/**
	 * Creates a digest hash for the given user and password.
	 *
	 * <p>Returns the hashed password or the plain password if the digest algorithm is not available.
	 * </p>
	 */
	private String buildDigestHash(User user, String password) {
		this.log.warn("Outdated password detected for user {}",
				user.getUsername());

		String passwordHashed;
		MessageDigest md;
		String hashFunction = user.getHashFunction();

		try {
			md = MessageDigest.getInstance(hashFunction);
			md.update(password.getBytes());
			md.update(user.getHashSalt().getBytes());
			passwordHashed = new HexBinaryAdapter().marshal(md.digest());
		} catch (NoSuchAlgorithmException e) {
			this.log.error("Unknown encryption '{}', assuming plaintext.",
					hashFunction);
			passwordHashed = password;
		}

		return passwordHashed;
	}

	/**
	 * Creates an activation code of the given length for the given user.
	 */
	private String createActivationCode(User user, int length) throws NoSuchAlgorithmException {
		// Activation key
		MessageDigest mdMd5 = MessageDigest.getInstance("MD5");
		// Create something difficult to predict: current time plus random number
		// TODO is this hard enough to predict?
		String activationKey = String.valueOf(new Date().getTime() + Math.round(Math.random() * 1000));

		mdMd5.update(activationKey.getBytes());
		mdMd5.update(user.getHashSalt().getBytes());

		// Convert activation code from binary to hex
		return new HexBinaryAdapter().marshal(mdMd5.digest()).substring(0, length);
	}

	/**
	 * Creates a hashed password from plain text for the given user.
	 */
	public void createPassword(User user, String passwordPlain) {

		// Salts
		String salt = BCrypt.gensalt(this.saltrounds);
		String saltDb = salt.substring(salt.length() - 10); // Only 10 chars in DB
		// Password
		String passwordHash = BCrypt.hashpw(passwordPlain, salt);

		user.setPasswordHash(passwordHash);
		user.setHashSalt(saltDb);
		user.setHashFunction("bcrypt");
	}

	/**
	 * Creates a password reset token for the given user.
	 */
	public User createPasswordResetToken(User user) throws NoSuchAlgorithmException {
		// If a code existed already, simply re-send the mail with the old code so that we don't
		// invalidate a previously sent mail
		if (user.getActivationCode() == null) {
			user.setActivationCode(this.createActivationCode(user, 10));
		}

		return user;
	}

	/**
	 * Creates a user from the given {@link UserForm}.
	 */
	@Transactional
	public User createUser(UserForm userForm) throws NoSuchAlgorithmException {

		User user = new User();
		this.createPassword(user, userForm.getPassword());

		user.setUsername(userForm.getUsername());
		user.setTitle(userForm.getTitle());
		// TODO make activation code length configurable
		user.setActivationCode(this.createActivationCode(user, 6));
		user.setEmail(userForm.getEmail());
		user.setForename(userForm.getForename());
		user.setSurname(userForm.getSurname());
		user.setTitle(userForm.getTitle());
		user.setRegistrationDate(new Date());
		user.setLocale(new Locale("en", "GB").toString());

		return this.userRepository.save(user);
	}

	/**
	 * Deactivates the given user.
	 */
	@Transactional
	public User deactivateUser(User user) {
		user.setActivationDate(null);

		return this.userRepository.save(user);
	}

	/**
	 * Returns the username or, if exists, the title as well as first and last name.
	 *
	 * TODO move to template service
	 */
	public String getUsernameOrTitleFirstLastName(User user) {
		String loggedInAs =
				((user.getTitle() != null ? user.getTitle() + " " : "")
						+ (user.getForename() != null ?
						user.getForename() + " " : "")
						+ (user.getSurname() != null ? user.getSurname() : ""))
						.trim().replaceAll(" +", " ");

		return loggedInAs.equals("") ? user.getUsername() : loggedInAs;
	}

	/**
	 * Returns whether the given new user is unique.
	 *
	 * <p>A user is unique if no user with the same username or e-mail address exists.</p>
	 */
	@Transactional(readOnly = true)
	public boolean isNewUserUnique(UserForm userForm) {
		return this.userRepository.countByUsernameOrEmail(userForm.getUsername(),
				userForm.getEmail()) == 0;
	}

	/**
	 * Returns whether the given technique is available to the given user according to their lab
	 * settings.
	 */
	@Transactional(readOnly = true)
	public boolean isTechniqueAvailableToUser(User user, Technique technique) {
		return this.profileRepository.findByUserAndTechniqueAndEnabledTrue(user, technique)
				.isPresent();
	}

	/**
	 * Loads not activated users.
	 */
	@Transactional(readOnly = true)
	public List<User> loadNotActivatedUsers() {
		return this.userRepository.findByActivationDateIsNull();
	}

	/**
	 * Loads users with the given IDs.
	 */
	@Transactional(readOnly = true)
	public List<User> loadUsersByIds(Integer[] ids) {
		return Lists.newArrayList(
				this.userRepository.findAll(Arrays.asList(ids)).iterator());
	}

	/**
	 * Removes the given role from the given user.
	 */
	@Transactional
	public User removeRole(User user, String role) {
		user.removeRole(role);

		return this.userRepository.save(user);
	}

	/**
	 * Sends activation confirmation mails to the given users.
	 */
	@Async
	public void sendActivationConfirmationMail(List<User> users) {
		Model model = new ExtendedModelMap();

		String[] emailAddresses = users.stream().map(User::getEmail)
				.toArray(String[]::new);

		this.serviceManager.getBean(MailService.class)
				.sendMail("mail/user_activated", emailAddresses,
						this.ts.translate("mail.user_activated.subject"), model);
	}

	/**
	 * Sends a password reset mail to the given user.
	 */
	@Async
	public void sendPasswordResetMail(User user) {
		Model model = new ExtendedModelMap();

		model.addAttribute(user);

		this.serviceManager.getBean(MailService.class)
				.sendMail("mail/user_password", new String[] { user.getEmail() },
						this.ts.translate("mail.user_password.subject"), model);
	}

	/**
	 * Updates the user with the given ID using the information from the given {@link User} object.
	 */
	public User updateUser(User user, Integer id) {
		// TODO solve without explicitly setting everything?
		User userPersisted = this.userRepository.findOne(id);
		userPersisted.setEmail(user.getEmail());
		//userPersisted.setForename(user.getForename());
		//userPersisted.setSurname(user.getSurname());
		userPersisted.setTitle(user.getTitle());

		// TODO solve cleaner
		if (user.getHashSalt() != null) {
			userPersisted.setPasswordHash(user.getPasswordHash());
			userPersisted.setHashFunction(user.getHashFunction());
			userPersisted.setHashSalt(user.getHashSalt());
		}

		userPersisted.setLocale(user.getLocale());
		this.userRepository.save(userPersisted);
		//userPersisted.setTechniques(user.getTechniques());

		return userPersisted;
	}

	/**
	 * Updates the given user using the given {@link UserForm} object.
	 *
	 * TODO unify user create/update processing
	 */
	public User updateUser(User user, UserForm userForm) {
		user.setEmail(userForm.getEmail());
		user.setForename(userForm.getForename());
		user.setSurname(userForm.getSurname());
		user.setTitle(userForm.getTitle());
		user.setUsername(userForm.getUsername());

		return this.userRepository.save(user);
	}

	/**
	 * Updates a user after login, setting session information resetting password reset tokens.
	 */
	public User updateUserAfterLogin(User user) {
		// TODO validation
		User userPersisted = this.userRepository
				.findFirstByUsername(user.getUsername()).get();
		Session session = this.securityRealm.getShiroSubject().getSession();
		// Get user and update his login information
		userPersisted.setLastIp(session.getHost());
		userPersisted.setLastLogin(new Date());
		userPersisted.setLastSession(userPersisted.getCurrentSession());
		userPersisted.setCurrentSession(session.getId().toString());
		// Remove password reset token
		userPersisted.setActivationCode(null);
		return this.userRepository.save(userPersisted);
	}
}
