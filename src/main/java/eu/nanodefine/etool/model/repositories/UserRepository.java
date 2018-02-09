/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.User;

/**
 * Repository for {@link User}s.
 */
public interface UserRepository extends CrudRepository<User, Integer> {

	/**
	 * Count the number of users with the given username or e-mail.
	 */
	Integer countByUsernameOrEmail(String username, String email);

	/**
	 * Returns users that have no activation date set.
	 */
	List<User> findByActivationDateIsNull();

	/**
	 * Find first by e-mail.
	 */
	Optional<User> findFirstByEmail(String email);

	/**
	 * Find first by username.
	 */
	Optional<User> findFirstByUsername(String username);
}
