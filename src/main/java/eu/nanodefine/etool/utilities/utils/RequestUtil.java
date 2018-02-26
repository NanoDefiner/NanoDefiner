/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Collection of static methods for analysis and debugging purpose of
 * {@link HttpServletRequest}s.
 *
 * TODO move to service
 */
public abstract class RequestUtil {

	/**
	 * Returns a string, listing all parameters and values of a given request.
	 *
	 * @param request given request
	 * @return string listing of all parameters and values
	 */
	public static String requestToString(HttpServletRequest request) {
		StringBuilder s = new StringBuilder();

		Map<String, String[]> map = request.getParameterMap();
		Set<String> keys = map.keySet();

		for (String param : keys) {
			for (int i = 0; i < map.get(param).length; i++) {
				s.append(param).append("=").append(map.get(param)[i]).append("\n");
			}
		}

		return s.toString();
	}

}
