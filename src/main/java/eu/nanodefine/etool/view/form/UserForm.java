/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.view.form;

import eu.nanodefine.etool.controller.controllers.user.UserCreateController;
import eu.nanodefine.etool.model.dto.User;

/**
 * Form-backing bean for the registration form.
 *
 * @see UserCreateController
 * TODO move to view package
 */
public class UserForm {

	private String email;

	private String forename;

	private String password;

	private String passwordRepeat;

	private String surname;

	private String title;

	private String username;

	public UserForm() {
	}

	public UserForm(String email, String forename, String password,
			String passwordRepeat, String surname, String title, String username) {
		this.email = email;
		this.forename = forename;
		this.password = password;
		this.passwordRepeat = passwordRepeat;
		this.surname = surname;
		this.title = title;
		this.username = username;
	}

	public UserForm(User user) {
		this(user.getEmail(), user.getForename(), null, null, user.getSurname(), user.getTitle(),
				user.getUsername());
	}

	public String getEmail() {
		return this.email;
	}

	public String getTitle() {
		return this.title;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getForename() {
		return this.forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordRepeat() {
		return this.passwordRepeat;
	}

	public void setPasswordRepeat(String passwordRepeat) {
		this.passwordRepeat = passwordRepeat;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getUsername() {
		return this.username;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
