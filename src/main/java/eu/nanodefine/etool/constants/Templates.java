/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.constants;

import java.io.File;

/**
 * Constant collection for thymeleaf template names.
 */
public interface Templates {

	String ADMIN = "admin" + File.separator;

	String ADMIN_LOCALE = ADMIN + "locale_coverage";

	String ADMIN_USER = ADMIN + "user";

	String CUSTOM_ATTRIBUTE = Entities.CUSTOM_ATTRIBUTE + File.separator;

	String CUSTOM_ATTRIBUTE_CREATE = CUSTOM_ATTRIBUTE + "create";

	String DASHBOARD = Entities.DASHBOARD;

	String DEBUG = Entities.DEBUG + File.separator;

	String DOSSIER = Entities.DOSSIER + File.separator;

	String DOSSIER_CREATE = DOSSIER + "create";

	String DOSSIER_DELETE = DOSSIER + "delete";

	String DOSSIER_LIST = DOSSIER + "list";

	String DOSSIER_READ = DOSSIER + "read";

	String ERROR = Entities.ERROR + File.separator;

	String ERROR_GENERIC = ERROR + "generic";

	String ERROR_NOT_FOUND = ERROR + "not_found";

	String ERROR_USER = ERROR + "user";

	String INDEX = "index";

	String INDEX_DEVEL = "devel";

	String ISSUE = Entities.ISSUE + File.separator;

	String ISSUE_LIST = ISSUE + Actions.LIST;

	String ISSUE_UPDATE = ISSUE + Actions.UPDATE;

	String MATERIAL = Entities.MATERIAL + File.separator;

	String MATERIAL_CREATE = MATERIAL + "create";

	String MATERIAL_LIST = MATERIAL + "list";

	String MATERIAL_READ = MATERIAL + "read";

	String METHOD = Entities.METHOD + File.separator;

	String METHOD_CREATE = METHOD + "create";

	String METHOD_LIST = METHOD + "list";

	String METHOD_READ = METHOD + "read";
	//String METHOD_DELETE = METHOD + "delete.jsp";

	String PDF = "pdf" + File.separator;

	String PDF_REPORT = PDF + "report";

	String PROFILE = "profile" + File.separator;

	String PROFILE_UPDATE = PROFILE + "update";

	String REPORT = "report" + File.separator;

	String REPORT_CREATE = REPORT + "create";

	String REPORT_LIST = REPORT + "list";

	String REPORT_READ = REPORT + "read";

	String USER = "user" + File.separator;

	String USER_CREATE = USER + "create";

	String USER_LOGIN = USER + "login";

	String USER_PASSWORD = USER + "password";

	String USER_PASSWORD_RESET = USER_PASSWORD + "_reset";

	String USER_UPDATE = USER + "update";
}
