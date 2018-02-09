/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Method attribute class.
 *
 * <p>A method attribute consists of a name and a value and belongs to a method.</p>
 *
 * <p>Method attributes are internally used attributes to represent the state of a method. Unlike
 * custom attributes, they are not directly visible to or changeable by the user.</p>
 */
@Entity
@Table(name = "MethodAttribute",
		uniqueConstraints = @UniqueConstraint(columnNames = { "fk_id_method", "name" }))
@IdClass(MethodAttributeId.class)
public class MethodAttribute {

	private Method method;

	private String name;

	private String value;

	public MethodAttribute() {
	}

	public MethodAttribute(Method method, String name) {

		this.method = method;
		this.name = name;
	}

	public MethodAttribute(Method method, String name, String value) {

		this.method = method;
		this.name = name;
		this.value = value;
	}

	/**
	 * Method attributes are equal if the have the same name and belong to the same method.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MethodAttribute)) {
			return false;
		}

		MethodAttribute methodAttribute = (MethodAttribute) o;

		return methodAttribute.getName().equals(this.getName()) &&
				methodAttribute.getMethod().equals(this.getMethod());
	}

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_method", nullable = false)
	public Method getMethod() {
		return this.method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	@Id
	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return (this.getMethod() != null ? this.getMethod().hashCode() : 0) +
				(this.getName() != null ? this.getName().hashCode() : 0);
	}
}
