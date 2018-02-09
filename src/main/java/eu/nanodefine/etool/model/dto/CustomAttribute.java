/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;

/**
 * Custom attribute entity.
 *
 * <p>A custom attribute consists of a name, a value, can be a file, and has a change date. It
 * belongs to an {@link ICustomAttributeEntity} and represents arbitrary
 * additional entity information (e.g. supporting evidence for method) which will be included in
 * the report.</p>
 */
@Entity
@Table(name = "CustomAttribute")
public class CustomAttribute implements IDataTransferObject {

	private Date changeDate;

	private String comment;

	private boolean file = false;

	private Integer id;

	private String name;

	private String value;

	public CustomAttribute() {
	}

	public CustomAttribute(String name, String value) {

		this.name = name;
		this.value = value;
	}

	public CustomAttribute(String comment, boolean file, String name, String value) {

		this.comment = comment;
		this.file = file;
		this.name = name;
		this.value = value;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "change_date", nullable = false, length = 19)
	public Date getChangeDate() {
		return this.changeDate;
	}

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

	@Override
	@Transient
	public String getEntityType() {
		return Entities.CUSTOM_ATTRIBUTE;
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

	@Column(name = "file", nullable = false)
	public boolean isFile() {
		return this.file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	@PreUpdate
	@PrePersist
	protected void updateTimestamps() {
		this.changeDate = new Date();
	}
}
