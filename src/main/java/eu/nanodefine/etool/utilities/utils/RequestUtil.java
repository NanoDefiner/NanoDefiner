/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import java.util.Iterator;
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
	 * @param request
	 *            given request
	 * @return string listing of all parameters and values
	 */
	public static String requestToString(HttpServletRequest request) {
		String s = new String();

		Map<String, String[]> map = request.getParameterMap();
		Set<String> keys = map.keySet();

		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String param = it.next();
			for (int i = 0; i < map.get(param).length; i++) {
				s += param + "=" + map.get(param)[i] + "\n";
			}
		}

		return s;
	}

}
