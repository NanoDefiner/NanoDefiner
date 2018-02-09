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
 * Report entity class.
 *
 * <p>A report consists of a name, report file, has a creation and change date and belongs to a
 * dossier.</p>
 *
 * <p>A report aggregates all dossier information in a PDF file. For each report, a set of methods
 * can be selected which will be included. The IDs of these methods are stored in the PDF report
 * file name.</p>
 */
@Entity
@Table(name = "Report")
public class Report
		implements java.io.Serializable, IDataTransferObject, IArchivable, IUserAwareEntity {

	private boolean archived = false;

	private Date changeDate;

	private Date creationDate;

	private Dossier dossier;

	private Integer id;

	private String name;

	private String reportFile;

	public Report() {
	}

	public Report(Dossier dossier, boolean archived, String name) {
		this.dossier = dossier;
		this.archived = archived;
		this.name = name;
	}

	public Report(Dossier dossier, boolean archived, String name, String reportFile) {
		this.dossier = dossier;
		this.archived = archived;
		this.name = name;
		this.reportFile = reportFile;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Report && DtoUtil.equals(this, o);
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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date", nullable = false, length = 19)
	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fk_dossier", nullable = false)
	public Dossier getDossier() {
		return this.dossier;
	}

	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.REPORT;
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

	@Column(name = "report_file")
	public String getReportFile() {
		return this.reportFile;
	}

	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}

	/**
	 * Returns the file name of the report with attachments.
	 *
	 * TODO extract into constant
	 */
	@Transient
	public String getReportFileWithAttachments() {
		return this.getReportFile()
				.substring(0, this.getReportFile().length() - 4) + "_attachments.pdf";
	}

	@Override
	@Transient
	public User getUser() {
		return this.dossier.getUser();
	}

	@Override
	@Transient
	public void setUser(User user) {
		this.dossier.setUser(user);
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				Report.class.hashCode() + this.id.hashCode() : super.hashCode();
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
