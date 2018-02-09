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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.IArchivable;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.model.interfaces.IUserAwareEntity;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * Issue entity class.
 *
 * <p>An issue consists of a text, comment, it has a creation and change date, and it belongs to a
 * user.</p>
 *
 * <p>An issue contains user feedback or error reports and comments can be changed by admins
 * only.</p>
 */
@Entity
@Table(name = "Issue")
public class Issue implements java.io.Serializable, IDataTransferObject,
		IArchivable, IUserAwareEntity {

	private boolean archived = false;

	private Date changeDate;

	private String comment;

	private Date creationDate;

	private Integer id;

	private String text;

	private User user;

	public Issue() {

	}

	public Issue(User user, String text) {
		this.user = user;
		this.text = text;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Issue && DtoUtil.equals(this, o);
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

	@Transient
	@Override
	public String getEntityType() {
		return Entities.ISSUE;
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

	@Column(name = "text")
	@Lob
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fk_user", nullable = true)
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
				Issue.class.hashCode() + this.id.hashCode() : super.hashCode();
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

	@PreUpdate
	@PrePersist
	protected void updateTimestamps() {
		this.changeDate = new Date();

		if (this.creationDate == null) {
			this.creationDate = this.changeDate;
		}
	}
}
