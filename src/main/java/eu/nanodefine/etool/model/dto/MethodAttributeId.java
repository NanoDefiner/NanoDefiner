/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import java.io.Serializable;

/**
 * Compound primary key class for {@link MethodAttribute}s.
 *
 * <p>{@link MethodAttribute}s do not have IDs, instead they are uniquely identifiable by the
 * combination of method and name, i.e. each method cannot have more than one attribute with the
 * same name.</p>
 */
public class MethodAttributeId implements Serializable {

	private Integer method;

	private String name;

	public MethodAttributeId() {
	}

	public MethodAttributeId(Integer method, String name) {

		this.method = method;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MethodAttributeId) {
			MethodAttributeId methodAttributeId = (MethodAttributeId) o;
			return this.name.equals(methodAttributeId.name)
					&& this.method.equals(methodAttributeId.method);
		} else {
			return false;
		}
	}

	public Integer getMethod() {
		return this.method;
	}

	public void setMethod(Integer method) {
		this.method = method;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return this.method.hashCode() + this.name.hashCode();
	}
}
