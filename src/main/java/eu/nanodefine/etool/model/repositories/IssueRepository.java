/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.repositories;

import org.springframework.data.repository.CrudRepository;

import eu.nanodefine.etool.model.dto.Issue;

/**
 * Repository for {@link Issue}s.
 */
public interface IssueRepository extends CrudRepository<Issue, Integer> {
}
