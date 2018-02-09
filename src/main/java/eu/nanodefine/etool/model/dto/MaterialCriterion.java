/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.dto;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.knowledge.ddos.Decidable;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.utilities.utils.DtoUtil;

/**
 * Material criterion entity class.
 *
 * <p>A material criterion has a name and value and belongs to a material.</p>
 *
 * <p>It represents a material attribute and corresponding value.</p>
 */
@Entity
@Table(name = "MaterialCriterion")
public class MaterialCriterion
		implements java.io.Serializable, IDataTransferObject, Decidable {

	private Integer id;

	private Material material;

	private String name;

	private String value;

	public MaterialCriterion() {
	}

	public MaterialCriterion(Material material, String name, String value) {
		this.material = material;
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MaterialCriterion && DtoUtil.equals(this, o);
	}

	@Transient
	@Override
	public String getEntityType() {
		return Entities.MATERIAL_CRITERION;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_material", nullable = false)
	public Material getMaterial() {
		return this.material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	@Override
	@Column(name = "name", nullable = false, length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Column(name = "value", nullable = false)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return this.id != null ?
				MaterialCriterion.class.hashCode() + this.id.hashCode() :
				super.hashCode();
	}
}
