/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.model.interfaces.IUserAwareEntity;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * User entity class.
 *
 * <p>A user consists of all the expected things: username, first/last name, email address, title.
 * Further, there is a bunch of data stored surrounding the user, like the last IP, last session
 * ID, current session ID, registration date, activation date, password hash, access string
 * (roles), locale, whether his login is remembered, etc. Access to the associated entities, i.e.
 * dossiers and materials, is also provided.</p>
 */
@Entity
@Table(name = "User", uniqueConstraints = {
		@UniqueConstraint(columnNames = "email"),
		@UniqueConstraint(columnNames = "username") })
public class User
		implements java.io.Serializable, IDataTransferObject, IUserAwareEntity {

	private String access;

	private String activationCode;

	private Date activationDate;

	private String currentSession;

	private Set<Dossier> dossiers = new HashSet<>(0);

	private String email;

	private String forename;

	private String hashFunction;

	private String hashSalt;

	private Integer id;

	private String lastIp;

	private Date lastLogin;

	private String lastSession;

	private String locale;

	private Set<Material> materials = new HashSet<>(0);

	private String passwordHash;

	private Set<Profile> profiles = new HashSet<>(0);

	private Date registrationDate;

	private boolean remembered;

	private String surname;

	private String title;

	private String username;

	public User() {
	}

	public User(String username, String passwordHash,
			String hashFunction, String hashSalt, Date registrationDate,
			String forename, String surname, String email, boolean remembered,
			String locale) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.hashFunction = hashFunction;
		this.hashSalt = hashSalt;
		this.registrationDate = registrationDate;
		this.forename = forename;
		this.surname = surname;
		this.email = email;
		this.remembered = remembered;
		this.locale = locale;
	}

	public User(String username, String passwordHash,
			String hashFunction, String hashSalt, Date registrationDate,
			Date activationDate, String activationCode, String forename,
			String surname, String title, String email, String currentSession,
			String lastSession, Date lastLogin, String lastIp,
			boolean remembered,
			String access, String locale, Set<Profile> profiles,
			Set<Dossier> dossiers, Set<Material> materials) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.hashFunction = hashFunction;
		this.hashSalt = hashSalt;
		this.registrationDate = registrationDate;
		this.activationDate = activationDate;
		this.activationCode = activationCode;
		this.forename = forename;
		this.surname = surname;
		this.title = title;
		this.email = email;
		this.currentSession = currentSession;
		this.lastSession = lastSession;
		this.lastLogin = lastLogin;
		this.lastIp = lastIp;
		this.remembered = remembered;
		this.access = access;
		this.locale = locale;
		this.profiles = profiles;
		this.dossiers = dossiers;
		this.materials = materials;
	}

	/**
	 * Adds the given role for this user.
	 */
	@Transient
	public void addRole(String role) {
		Set<String> roles = this.getRoles();
		roles.add(role);
		this.setAccess(roles.stream().collect(Collectors.joining(",")));
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof User && DtoUtil.equals(this, o);
	}

	@Column(name = "access")
	public String getAccess() {
		return this.access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	@Column(name = "activation_code", length = 10)
	public String getActivationCode() {
		return this.activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "activation_date", length = 19)
	public Date getActivationDate() {
		return this.activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	@Column(name = "current_session", length = 50)
	public String getCurrentSession() {
		return this.currentSession;
	}

	public void setCurrentSession(String currentSession) {
		this.currentSession = currentSession;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Dossier> getDossiers() {
		return this.dossiers;
	}

	public void setDossiers(Set<Dossier> dossiers) {
		this.dossiers = dossiers;
	}

	@Column(name = "email", unique = true, nullable = false, length = 50)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.USER;
	}

	@Column(name = "forename", nullable = false, length = 50)
	public String getForename() {
		return this.forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	@Column(name = "hash_function", nullable = false, length = 10)
	public String getHashFunction() {
		return this.hashFunction;
	}

	public void setHashFunction(String hashFunction) {
		this.hashFunction = hashFunction;
	}

	@Column(name = "hash_salt", nullable = false, length = 10)
	public String getHashSalt() {
		return this.hashSalt;
	}

	public void setHashSalt(String hashSalt) {
		this.hashSalt = hashSalt;
	}

	@Override
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "last_ip", length = 50)
	public String getLastIp() {
		return this.lastIp;
	}

	public void setLastIp(String lastIp) {
		this.lastIp = lastIp;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_login", length = 19)
	public Date getLastLogin() {
		return this.lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Column(name = "last_session", length = 50)
	public String getLastSession() {
		return this.lastSession;
	}

	public void setLastSession(String lastSession) {
		this.lastSession = lastSession;
	}

	@Column(name = "locale", nullable = false, length = 5)
	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Material> getMaterials() {
		return this.materials;
	}

	public void setMaterials(Set<Material> materials) {
		this.materials = materials;
	}

	@Column(name = "password_hash", nullable = false)
	public String getPasswordHash() {
		return this.passwordHash;
	}

	public void setPasswordHash(String credentialHash) {
		this.passwordHash = credentialHash;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = { CascadeType.ALL })
	public Set<Profile> getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Set<Profile> profiles) {
		this.profiles = profiles;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "registration_date", nullable = false, length = 19)
	public Date getRegistrationDate() {
		return this.registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	/**
	 * Returns the roles of the user.
	 *
	 * <p>The roles are extracted from the {@code access} field, updating the resulting set does
	 * not update the roles of the user.</p>
	 */
	@Transient
	public Set<String> getRoles() {
		return Arrays.stream(this.getAccess() != null ?
				this.getAccess().split(",") :
				new String[] {}).collect(Collectors.toSet());
	}

	@Column(name = "surname", nullable = false, length = 50)
	public String getSurname() {
		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@Column(name = "title", length = 10)
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	@Transient
	public User getUser() {
		return this;
	}

	@Override
	@Transient
	public void setUser(User user) {
		throw new RuntimeException(new OperationNotSupportedException());
	}

	@Column(name = "username", unique = true, nullable = false, length = 50)
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				User.class.hashCode() + this.id.hashCode() : super.hashCode();
	}

	/**
	 * Returns whether this user is activated.
	 *
	 * A {@link User} is considered activated if its {@link User#getActivationDate()} is
	 * not {@literal null}.
	 */
	@Transient
	public boolean isActivated() {
		return this.getActivationDate() != null;
	}

	@Column(name = "remembered", nullable = false)
	public boolean isRemembered() {
		return this.remembered;
	}

	public void setRemembered(boolean remembered) {
		this.remembered = remembered;
	}

	/**
	 * Removes the given role from this user.
	 *
	 * @return true if the role was removed, false if the user didn't have the role
	 */
	@Transient
	public boolean removeRole(String role) {
		Set<String> roles = this.getRoles();
		boolean removed = roles.remove(role);
		this.setAccess(roles.stream().collect(Collectors.joining(",")));

		return removed;
	}
}
