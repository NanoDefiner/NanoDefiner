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
 * Dossier entity class.
 *
 * <p>A dossier consists of a name, purpose, comment, internal comment, sample name, materials,
 * methods, reports, custom attributes, it has a creation and change date, it belongs to a user
 * and it can be archived.</p>
 *
 * <p>A dossier is the highest level entity in the system and describes a complete material
 * classification process.</p>
 */
@Entity
@Table(name = "Dossier")
public class Dossier
		implements java.io.Serializable, ICustomAttributeEntity, IArchivable {

	private boolean archived = false;

	private Date changeDate;

	private String comment;

	private Date creationDate;

	private Set<CustomAttribute> customAttributes;

	private Integer id;

	private String internalComment;

	private Set<Material> materials = new HashSet<>(0);

	private Set<Method> methods = new HashSet<>(0);

	private boolean multiconstituent = false;

	private String name;

	private String purpose;

	private Set<Report> reports = new HashSet<>(0);

	private String sampleName;

	private User user;

	public Dossier() {
	}

	public Dossier(User user, String name, boolean multiconstituent, boolean archived,
			String purpose) {
		this.user = user;
		this.name = name;
		this.multiconstituent = multiconstituent;
		this.archived = archived;
		this.purpose = purpose;
	}

	public Dossier(User user, String name, String comment, String internalComment,
			String sampleName, boolean multiconstituent, boolean archived, String purpose,
			Set<Material> materials, Set<Method> methods, Set<Report> reports) {
		this.user = user;
		this.name = name;
		this.comment = comment;
		this.internalComment = internalComment;
		this.sampleName = sampleName;
		this.purpose = purpose;
		this.multiconstituent = multiconstituent;
		this.archived = archived;
		this.materials = materials;
		this.methods = methods;
		this.reports = reports;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Dossier && DtoUtil.equals(this, o);
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
	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = {
			CascadeType.ALL })
	@JoinTable(name = "Dossier_CustomAttribute", joinColumns = {
			@JoinColumn(name = "id_fk_dossier", nullable = false, updatable = false) }, inverseJoinColumns = {
			@JoinColumn(name = "id_fk_custom_attribute", nullable = false, updatable = false) })
	public Set<CustomAttribute> getCustomAttributes() {
		return this.customAttributes;
	}

	@Override
	public void setCustomAttributes(
			Set<CustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.DOSSIER;
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

	@Column(name = "internal_comment")
	@Lob
	public String getInternalComment() {
		return this.internalComment;
	}

	public void setInternalComment(String internalComment) {
		this.internalComment = internalComment;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dossier", cascade = {
			CascadeType.ALL })
	public Set<Material> getMaterials() {
		return this.materials;
	}

	public void setMaterials(Set<Material> materials) {
		this.materials = materials;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dossier", cascade = {
			CascadeType.ALL })
	public Set<Method> getMethods() {
		return this.methods;
	}

	public void setMethods(Set<Method> methods) {
		this.methods = methods;
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

	@Column(name = "purpose", nullable = false, length = 50)
	public String getPurpose() {
		return this.purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dossier", cascade = {
			CascadeType.ALL })
	public Set<Report> getReports() {
		return this.reports;
	}

	public void setReports(Set<Report> reports) {
		this.reports = reports;
	}

	@Column(name = "sample_name")
	public String getSampleName() {
		return this.sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_user", nullable = false)
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
				Dossier.class.hashCode() + this.id.hashCode() : super.hashCode();
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

	@Column(name = "multiconstituent", nullable = false)
	public boolean isMulticonstituent() {
		return this.multiconstituent;
	}

	public void setMulticonstituent(boolean multiconstituent) {
		this.multiconstituent = multiconstituent;
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
