/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Constant collection for actions.
 */
@Component("A")
public class Actions {

	private Logger log = LoggerFactory.getLogger(Actions.class);

	/*
	 * Basic CRUD actions
	 */

	public static final String CREATE = "create";

	public static final String READ = "read";

	public static final String UPDATE = "update";

	public static final String DELETE = "delete";

	/*
	 * Non-basic actions
	 */

	public static final String LIST = "list";

	public static final String LOGIN = "login";

	public static final String LOGOUT = "logout";

	public static final String ACTIVATE = "activate";

	public static final String ARCHIVE = "archive";
}
