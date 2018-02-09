/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.IArchivable;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * Material entity class.
 *
 * <p>A material consists of a name, comment, signifier, material criteria, custom attributes, can
 * be a template and reference material, belongs to a user and (if no template) dossier and has a
 * creation and change date.</p>
 *
 * <p>Material properties are stored using {@link MaterialCriterion}s which are used to determine
 * recommended techniques.</p>
 */
@Entity
@Table(name = "Material")
public class Material
		implements java.io.Serializable, ICustomAttributeEntity, IArchivable {

	private boolean archived = false;

	private Date changeDate;

	private String comment;

	private Date creationDate;

	private Set<CustomAttribute> customAttributes;

	private Dossier dossier;

	private Integer id;

	private Set<MaterialCriterion> materialCriterions = new HashSet<>(0);

	private String name;

	private boolean reference = false;

	private String signifier;

	private boolean template = false;

	private User user;

	public Material() {
	}

	public Material(String signifier, Dossier dossier, User user, String name, boolean archived,
			boolean template, boolean reference) {
		this.signifier = signifier;
		this.dossier = dossier;
		this.user = user;
		this.name = name;
		this.archived = archived;
		this.template = template;
		this.reference = reference;
	}

	public Material(String signifier, Dossier dossier, User user, String name, String comment,
			boolean archived, boolean template, boolean reference,
			Set<MaterialCriterion> materialCriterions) {
		this.signifier = signifier;
		this.dossier = dossier;
		this.user = user;
		this.name = name;
		this.comment = comment;
		this.archived = archived;
		this.template = template;
		this.reference = reference;
		this.materialCriterions = materialCriterions;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Material && DtoUtil.equals(this, o);
	}

	@Override
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "change_date", nullable = false, length = 19)
	public Date getChangeDate() {
		return this.changeDate;
	}

	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Column(name = "comment")
	@Lob
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date", nullable = false, length = 19)
	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.ALL })
	@JoinTable(name = "Material_CustomAttribute", joinColumns = {
			@JoinColumn(name = "id_fk_material", nullable = false, updatable = false) },
			inverseJoinColumns = {
					@JoinColumn(name = "id_fk_custom_attribute", nullable = false, updatable = false) })
	public Set<CustomAttribute> getCustomAttributes() {
		return this.customAttributes;
	}

	@Override
	public void setCustomAttributes(
			Set<CustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fk_dossier", nullable = true)
	public Dossier getDossier() {
		return this.dossier;
	}

	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.MATERIAL;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "material", cascade = {
			CascadeType.ALL })
	public Set<MaterialCriterion> getMaterialCriterions() {
		return this.materialCriterions;
	}

	public void setMaterialCriterions(Set<MaterialCriterion> materialCriterions) {
		this.materialCriterions = materialCriterions;
	}

	@Override
	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "signifier", nullable = false, length = 50)
	public String getSignifier() {
		return this.signifier;
	}

	public void setSignifier(String signifier) {
		this.signifier = signifier;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fk_user", nullable = false)
	public User getUser() {
		return this.user;
	}

	@Override
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				Material.class.hashCode() + this.id.hashCode() : super.hashCode();
	}

	@Override
	@Column(name = "archived", nullable = false)
	public boolean isArchived() {
		return this.archived;
	}

	@Override
	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Column(name = "reference", nullable = false)
	public boolean isReference() {
		return this.reference;
	}

	public void setReference(boolean reference) {
		this.reference = reference;
	}

	@Column(name = "template", nullable = false)
	public boolean isTemplate() {
		return this.template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	@PreUpdate
	@PrePersist
	protected void updateTimestamps() {
		this.changeDate = new Date();

		if (this.creationDate == null) {
			this.creationDate = this.changeDate;
		}
	}
}
