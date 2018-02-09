/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
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
 * Method entity class.
 *
 * <p>A method consists of a name, comment, data file and format, preparation and preparation
 * comment, result, tier, technique uncertainty, custom attributes, method attributes, is
 * associated with a technique and has a creation and change date.</p>
 *
 * <p>It represents a method used to analyse a material with the goal of classifying it.</p>
 */
@Entity
@Table(name = "Method")
public class Method
		implements java.io.Serializable, ICustomAttributeEntity, IArchivable {

	private boolean archived = false;

	private Date changeDate;

	private String comment;

	private Date creationDate;

	private Set<CustomAttribute> customAttributes;

	private String dataFile;

	private String dataFormat;

	private Dossier dossier;

	private Integer id;

	private Set<MethodAttribute> methodAttributes;

	private String name;

	private String preparation;

	private String preparationComment;

	private String result;

	private Technique technique;

	private Double techniqueUncertainty;

	private String tier;

	public Method() {
	}

	public Method(Technique technique, Dossier dossier, boolean archived, String name, String tier) {
		this.technique = technique;
		this.dossier = dossier;
		this.archived = archived;
		this.name = name;
		this.tier = tier;
	}

	public Method(Technique technique, Dossier dossier, boolean archived, String name,
			String comment, String tier, String dataFile, String dataFormat, String result,
			String preparation, String preparationComment, Double techniqueUncertainty,
			Set<MethodAttribute> methodAttributes) {
		this.technique = technique;
		this.dossier = dossier;
		this.archived = archived;
		this.name = name;
		this.comment = comment;
		this.tier = tier;
		this.dataFile = dataFile;
		this.dataFormat = dataFormat;
		this.result = result;
		this.preparation = preparation;
		this.preparationComment = preparationComment;
		this.techniqueUncertainty = techniqueUncertainty;
		this.methodAttributes = methodAttributes;
	}

	@Transient
	public boolean addAttribute(String name) {
		return this.addAttribute(name, null);
	}

	@Transient
	public boolean addAttribute(String name, String value) {
		return this.getAttributes().add(new MethodAttribute(this, name, value));
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Method && DtoUtil.equals(this, o);
	}

	/**
	 * Returns the method attribute for the given name, or null if it does not exist.
	 */
	@Transient
	public MethodAttribute getAttribute(String name) {
		for (MethodAttribute attribute : this.getAttributes()) {
			if (attribute.getName().equals(name)) {
				return attribute;
			}
		}

		return null;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "method", orphanRemoval = true,
			cascade = { CascadeType.ALL })
	public Set<MethodAttribute> getAttributes() {
		return this.methodAttributes;
	}

	public void setAttributes(Set<MethodAttribute> methodAttributes) {
		this.methodAttributes = methodAttributes;
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
	@JoinTable(name = "Method_CustomAttribute", joinColumns = {
			@JoinColumn(name = "id_fk_method", nullable = false, updatable = false) },
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

	/**
	 * TODO rename to analysis_data, it does not necessarily contain information about a file
	 */
	@Column(name = "data_file")
	public String getDataFile() {
		return this.dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * TODO rename to analysis_data_format
	 */
	@Column(name = "data_file_format")
	public String getDataFormat() {
		return this.dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_dossier", nullable = false)
	public Dossier getDossier() {
		return this.dossier;
	}

	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.METHOD;
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

	@Override
	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a numeric representation of the method result.
	 *
	 * <p>If the method does not have a result, {@code 0} is returned.</p>
	 */
	@Transient
	public Double getNumericResult() {
		try {
			return Double.valueOf(this.result);
		} catch (NumberFormatException e) {
			return 0.;
		}
	}

	@Column(name = "preparation")
	public String getPreparation() {
		return this.preparation;
	}

	public void setPreparation(String preparation) {
		this.preparation = preparation;
	}

	@Column(name = "preparation_comment")
	@Lob
	public String getPreparationComment() {
		return this.preparationComment;
	}

	public void setPreparationComment(String preparationComment) {
		this.preparationComment = preparationComment;
	}

	@Column(name = "result", length = 10)
	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_technique", nullable = false)
	public Technique getTechnique() {
		return this.technique;
	}

	public void setTechnique(Technique technique) {
		this.technique = technique;
	}

	@Column(name = "technique_uncertainty", nullable = false)
	public Double getTechniqueUncertainty() {
		return this.techniqueUncertainty;
	}

	public void setTechniqueUncertainty(Double techniqueUncertainty) {
		this.techniqueUncertainty = techniqueUncertainty;
	}

	@Column(name = "tier", nullable = false, length = 20)
	public String getTier() {
		return this.tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
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

	@Transient
	public boolean hasAttribute(String name) {
		return this.getAttributes().contains(new MethodAttribute(this, name));
	}

	/**
	 * Returns whether the method has a result.
	 *
	 * TODO can the result be 0? If not, remove second part of the condition
	 */
	@Transient
	public boolean hasResult() {
		return this.result != null && this.getNumericResult() > 0; // TODO
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				Method.class.hashCode() + this.id.hashCode() : super.hashCode();
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

	/**
	 * Removes the method attribute with the given name.
	 *
	 * @return Whether the attribute was removed.
	 */
	@Transient
	public boolean removeAttribute(String name) {
		return this.getAttributes().remove(new MethodAttribute(this, name));
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
