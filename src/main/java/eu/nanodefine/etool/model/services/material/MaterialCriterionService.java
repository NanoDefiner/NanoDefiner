/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.Entities;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredReference;
import eu.nanodefine.etool.knowledge.configurations.beans.ReferenceCriterion;
import eu.nanodefine.etool.knowledge.ddos.Decidable;
import eu.nanodefine.etool.knowledge.ddos.MaterialCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDO;
import eu.nanodefine.etool.knowledge.ddos.PerformanceCriterionDDOMap;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.ReferenceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.beans.Attribute;
import eu.nanodefine.etool.model.dto.Material;
import eu.nanodefine.etool.model.dto.MaterialCriterion;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.DroolsService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;
import eu.nanodefine.etool.utilities.utils.ExplanationUtil;
import eu.nanodefine.etool.view.material.MaterialCriterionTechniqueCompatibility;

/**
 * Service for material criterion-related processing.
 */
@Service
public class MaterialCriterionService implements IService {

	private Logger log = LoggerFactory.getLogger(MaterialCriterionService.class);

	@Autowired
	private PerformanceDictionary performanceDictionary;

	@Autowired
	private ReferenceDictionary referenceDictionary;

	@Autowired
	private ServiceManager serviceManager;

	@Resource(name = "techniques")
	private List<Technique> techniques;

	public Map<String, PerformanceCriterionDDOMap> convertPerformanceCriterionDDOMapListToMap(
			List<PerformanceCriterionDDOMap> matches) {
		return Maps.uniqueIndex(matches, PerformanceCriterionDDOMap::getTechniqueSignifier);
	}

	/**
	 * Creates a map of material criteria of the given material.
	 *
	 * <p>This method is used when loading a user-defined material template into the MCS.</p>
	 *
	 * TODO merge or more clearly separate this and #createMaterialCriteriaMap
	 */
	public Map<String, Object[]> createMaterialCriteriaFormMap(Material material) {

		Map<String, Object[]> materialCriteria = new HashMap<>();
		MaterialCriterionService mcs = this.serviceManager
				.getBean(MaterialCriterionService.class);

		for (MaterialCriterion mc : material.getMaterialCriterions()) {
			materialCriteria.put(Entities.MATERIAL + "." + mc.getName(),
					mcs.toArray(mc));
		}

		return materialCriteria;
	}

	/**
	 * Creates a map of material criteria for the given reference material.
	 *
	 * <p>This method is used when loading a reference material into the MCS.</p>
	 *
	 * @see #createMaterialCriteriaFormMap(Material)
	 */
	public Map<String, Object[]> createMaterialCriteriaFormMap(ConfiguredReference reference) {
		Map<String, Object[]> materialCriteria = new HashMap<>();
		MaterialCriterionService mcs = this.serviceManager
				.getBean(MaterialCriterionService.class);

		for (ReferenceCriterion rc : reference.getEntries()) {
			materialCriteria.put(Entities.MATERIAL + "." + rc.getName(),
					mcs.toArray(rc));
		}

		return materialCriteria;
	}

	/**
	 * Creates a set of {@link MaterialCriterion}s from a map of request parameters.
	 *
	 * <p>This method is used to convert MCS form parameters into a set of
	 * {@link MaterialCriterion}s</p>
	 */
	public Set<MaterialCriterion> createMaterialCriteriaFromRequestParameters(
			Map<String, String[]> parameterMap) {

		Set<MaterialCriterion> materialCriteria = new HashSet<>();

		// Each parameter name is prefixed by "material."
		int prefixLength = (Entities.MATERIAL + ".").length();
		Attribute a;
		String key;
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {

			key = entry.getKey();

			// If the key is shorter than the prefix, it's not one of our parameters
			if (key.length() <= prefixLength) {
				continue;
			}

			a = this.performanceDictionary.getAttribute(key.substring(prefixLength));

			if (a != null) {

				materialCriteria.add(ConfigurationUtil.createMaterialCriterion(entry.getValue(), a));
			}
		}

		return materialCriteria;
	}

	/**
	 * Creates a map of {@link MaterialCriterion} identifiers and values.
	 *
	 * The created map matches {@link MaterialCriterion} idenfiers (i.e.
	 * {@link MaterialCriterion#getName()} to translated values (i.e.
	 * {@link MaterialCriterion#getValue()} translated using
	 * {@link PerformanceDictionary#translateValue(String, String)}).
	 *
	 * @return Model map containing the material, the dossier, and the material
	 * criteria map.
	 */
	public Map<String, String> createMaterialCriteriaMap(Material material,
			boolean translate) {

		Map<String, String> materialCriteria = new HashMap<>();

		for (MaterialCriterion mc : material.getMaterialCriterions()) {
			materialCriteria.put(
					mc.getName(), translate ? this.performanceDictionary
							.translateValue(mc.getName(), mc.getValue()) : mc.getValue());
		}

		return materialCriteria;
	}

	/**
	 * Creates a material/technique compatibility table based on the given material criteria.
	 *
	 * @see #createMaterialCriteriaTechniqueCompatibilityTable(Set, Map)
	 */
	public Table<String, String, MaterialCriterionTechniqueCompatibility> createMaterialCriteriaTechniqueCompatibilityTable(
			Set<MaterialCriterion> materialCriteria) {

		return this.createMaterialCriteriaTechniqueCompatibilityTable(materialCriteria,
				this.convertPerformanceCriterionDDOMapListToMap(
						this.serviceManager.getBean(DroolsService.class)
								.performMatching(materialCriteria)));
	}

	/**
	 * Returns a table of material criteria names, technique signifiers and a compatibility measure.
	 *
	 * <p>This method creates a material/technique compatibility table based on the given material
	 * and performance criteria.</p>
	 *
	 * @see #createMaterialCriteriaTechniqueCompatibilityTable(Set)
	 */
	public Table<String, String, MaterialCriterionTechniqueCompatibility>
	createMaterialCriteriaTechniqueCompatibilityTable(
			Set<MaterialCriterion> materialCriteria,
			Map<String, PerformanceCriterionDDOMap> ddoMaps) {

		MaterialService ms = this.serviceManager.getBean(MaterialService.class);
		Table<String, String, MaterialCriterionTechniqueCompatibility> table =
				HashBasedTable.create(materialCriteria.size(), this.techniques.size());

		String explanation;
		PerformanceCriterionDDO pddo;
		MaterialCriterionDDO mddo;
		for (MaterialCriterion mc : materialCriteria) {
			mddo = new MaterialCriterionDDO(mc);

			// Add compatibility for each technique
			for (Technique t : this.techniques) {
				if (!ddoMaps.containsKey(t.getSignifier())) {
					this.log.warn("No match information for technique '{}'", t.getSignifier());
					continue;
				}

				pddo = ddoMaps.get(t.getSignifier()).get(mc.getName());

				explanation = ms.determineExplanation(pddo);

				if (mddo.isUnknown()) {
					explanation += "<br />" + ExplanationUtil
							.getIncompletenessDissuasionString(pddo, true);
				}

				table.put(mc.getName(), t.getSignifier(),
						new MaterialCriterionTechniqueCompatibility(mddo, pddo, explanation));
			}
		}

		return table;
	}

	/**
	 * Creates a map of compatibility tables for a collection of materials.
	 */
	public Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>
	createMaterialCriteriaTechniqueCompatibilityTableMap(
			Iterable<Material> materials) {

		Map<Material, Table<String, String, MaterialCriterionTechniqueCompatibility>>
				compatibilityTableMap = new HashMap<>();

		for (Material material : materials) {

			compatibilityTableMap.put(material,
					this.createMaterialCriteriaTechniqueCompatibilityTable(
							material.getMaterialCriterions()));
		}

		return compatibilityTableMap;
	}

	/**
	 * Returns an array representing the material criterion value(s).
	 *
	 * @see #toArray(Decidable, Attribute)
	 */
	public Object[] toArray(MaterialCriterion mc) {
		Attribute attribute = this.performanceDictionary.getAttribute(mc.getName());

		return this.toArray(mc, attribute);
	}

	/**
	 * Returns an array representing the reference criterion value(s).
	 *
	 * @see #toArray(Decidable, Attribute)
	 */
	public Object[] toArray(ReferenceCriterion rc) {
		Attribute attribute = this.referenceDictionary.getAttribute(rc.getName());

		return this.toArray(rc, attribute);
	}

	/**
	 * Returns an array representation of the value of the given decidable.
	 *
	 * <p>The {@code attribute} is used to determine the type of the decidable and how its value will
	 * be converted to an array. The values are used for form deserialisation.</p>
	 */
	private Object[] toArray(Decidable d, Attribute attribute) {

		switch (attribute.getType()) {
			case Attribute.TYPE_BINARY:
			case Attribute.TYPE_DECIMAL:
			case Attribute.TYPE_SCALE:
			case Attribute.TYPE_STRING:
				return new Object[] { d.getValue() };
			case Attribute.TYPE_INTERVAL:
				// First entry is the unknown checkbox value
				if (d.getValue().equals(ConfigurationUtil.UNKNOWN)) {
					return new Object[] { "?" };
				}
				return ArrayUtils.addAll(new Object[] { false },
						(Object[]) ConfigurationUtil.toInterval(d.getValue()));
			case Attribute.TYPE_SET:
				Set<String> values = ConfigurationUtil.toSet(d.getValue());
				if (values.isEmpty()) {
					values.add("");
				}
				return values.toArray();
			default:
				return new Object[] { "" };
		}

	}
}
