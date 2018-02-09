/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO Documentation
 *
 * TODO move to service
 */
public abstract class ReflectionUtil {

	/**
	 * TODO Documentation
	 *
	 * @param constant
	 * @param clazz
	 * @return
	 */
	public static Boolean isConstantOfClass(String constant, Class<?> clazz) {
		return getClassConstants(clazz).contains(constant);
	}

	/**
	 * TODO Documentation
	 *
	 * @param constant
	 * @param clazz
	 * @param lowerCase
	 * @return
	 */
	public static Boolean isConstantOfClass(String constant, Class<?> clazz,
			Boolean lowerCase) {
		return getClassConstants(clazz, lowerCase).contains(constant);
	}

	/**
	 * TODO Documentation
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<String> getClassConstants(Class<?> clazz) {
		return getClassConstants(clazz, Boolean.FALSE);
	}

	/**
	 * TODO Documentation
	 *
	 * @param clazz
	 * @param lowerCase
	 * @return
	 */
	public static Set<String> getClassConstants(Class<?> clazz,
			Boolean lowerCase) {
		Set<String> set = new HashSet<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
				//list.add((lowerCase) ? field.getName().toLowerCase() : field.getName());
				try {
					set.add((lowerCase) ? ((String) field.get(null))
							.toLowerCase() : (String) field.get(null));
				} catch (IllegalAccessException e) {
					// TODO: handle exception
				}
			}
		}
		return set;
	}

}
