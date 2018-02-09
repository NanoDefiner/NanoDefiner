/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * Technique profile class.
 *
 * <p>A profile consists of a duration and cost, can be enabled or disabled, belongs to a user and
 * is associated with a technique.</p>
 *
 * <p>A profile stores user-specific technique settings used for recommendation.</p>
 *
 * TODO use compound primary key?
 */
@Entity
@Table(name = "Profile")
public class Profile implements java.io.Serializable, IDataTransferObject {

	private Double cost;

	private Double duration;

	private boolean enabled = true;

	private Integer id;

	private Technique technique;

	private Double uncertainty;

	private User user;

	public Profile() {
	}

	public Profile(Technique technique, User user) {
		this.technique = technique;
		this.user = user;
	}

	public Profile(Technique technique, User user, boolean enabled, Double cost,
			Double duration, Double uncertainty) {
		this.cost = cost;
		this.duration = duration;
		this.enabled = enabled;
		this.technique = technique;
		this.user = user;
		this.uncertainty = uncertainty;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Profile && DtoUtil.equals(this, o);

	}

	@Column(name = "cost")
	public Double getCost() {
		return this.cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	@Column(name = "duration")
	public Double getDuration() {
		return this.duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.PROFILE;
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_technique", nullable = false)
	public Technique getTechnique() {
		return this.technique;
	}

	public void setTechnique(Technique technique) {
		this.technique = technique;
	}

	@Column(name = "uncertainty")
	public Double getUncertainty() {
		return this.uncertainty;
	}

	public void setUncertainty(Double uncertainty) {
		this.uncertainty = uncertainty;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_id_user", nullable = false)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				Method.class.hashCode() + this.id.hashCode() : super.hashCode();
	}

	@Column(name = "enabled", nullable = false)
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
