/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * Technique class.
 *
 * <p>A technique consists of a name, comment, and signifier, and is associated to a set of
 * methods.</p>
 *
 * <p>Techniques are loaded from the technique sheet. New techniques are automatically added and
 * existing techniques are updated using the technique sheet values, but existing techniques are
 * never removed to provide backwards-compatibility for existing methods.</p>
 *
 * TODO remove techniques if they are not in the KB and there are no associated methods?
 */
@Entity
@Table(name = "Technique")
public class Technique
		implements java.io.Serializable, IDataTransferObject {

	private String comment;

	private Integer id;

	private Set<Method> methods = new HashSet<>(0);

	private String name;

	private Set<Profile> profiles = new HashSet<>(0);

	private String signifier;

	public Technique() {
	}

	public Technique(String signifier, String name, String comment) {
		this.signifier = signifier;
		this.name = name;
		this.comment = comment;
	}

	public Technique(String signifier, String name,
			String comment, Set<Method> methods, Set<Profile> profiles) {
		this.signifier = signifier;
		this.name = name;
		this.comment = comment;
		this.methods = methods;
		this.profiles = profiles;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Technique && DtoUtil.equals(this, o);
	}

	@Column(name = "comment")
	@Lob
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.TECHNIQUE;
	}

	@Override
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "technique", cascade = {
			CascadeType.ALL })
	public Set<Method> getMethods() {
		return this.methods;
	}

	public void setMethods(Set<Method> methods) {
		this.methods = methods;
	}

	@Column(name = "name", nullable = false, length = 255)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "technique", cascade = { CascadeType.ALL })
	public Set<Profile> getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Set<Profile> profiles) {
		this.profiles = profiles;
	}

	@Column(name = "signifier", nullable = false, length = 50)
	public String getSignifier() {
		return this.signifier;
	}

	public void setSignifier(String signifier) {
		this.signifier = signifier;
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				Technique.class.hashCode() + this.id.hashCode() : super.hashCode();
	}
}
