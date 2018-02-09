/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.technique;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for technique hash-related processing.
 *
 * <p>Needed during application configuration and not later on.</p>
 *
 * @see eu.nanodefine.etool.config.KnowledgeConfig
 */
@Service
public class TechniqueHashService implements IService {

	/**
	 * Location of the file containing the technique hashes.
	 */
	@Value("${knowledge.hashes}")
	private String hashFileLocation;

	/**
	 * Creates a property key for the given technique and material signifier.
	 */
	public String buildPropertyKey(String techniqueSignifier, String materialSignifier) {
		return techniqueSignifier + "||" + materialSignifier;
	}

	/**
	 * Create a hash for the given technique.
	 *
	 * <p>The hash incorporates all values of the technique properties.</p>
	 */
	public String createTechniqueHash(ConfiguredPerformance cp,
			PerformanceDictionary performanceDictionary) throws NoSuchAlgorithmException {
		// Order elements to ensure reproducible hashes
		List<String> attributeNames = new ArrayList<>(performanceDictionary.getAttributeNames());
		Collections.sort(attributeNames);

		MessageDigest md = MessageDigest.getInstance("MD5");
		for (String attributeName : attributeNames) {
			md.update(cp.getEntry(attributeName).getValue().getBytes());
		}

		return new HexBinaryAdapter().marshal(md.digest());
	}

	/**
	 * Creates the technique hashes properties file.
	 *
	 * <p>The file will contain a mapping of technique signifiers to technique hashes.</p>
	 *
	 * @see #createTechniqueHash(ConfiguredPerformance, PerformanceDictionary)
	 */
	public void createTechniqueHashesFile(List<Technique> techniques,
			Resource resource,
			PerformanceConfiguration performanceConfiguration,
			PerformanceDictionary performanceDictionary) throws NoSuchAlgorithmException, IOException {
		Properties properties = new Properties();

		String propertyKey;
		for (Technique technique : techniques) {
			for (ConfiguredPerformance cp : performanceConfiguration
					.getEntriesForTechniqueSignifier(technique.getSignifier())) {

				propertyKey = this.buildPropertyKey(technique.getSignifier(),
						cp.getEntry(PerformanceAttributes.MATERIAL_SIGNIFIER).getValue());

				properties.setProperty(propertyKey,
						this.createTechniqueHash(cp, performanceDictionary));
			}
		}

		properties.store(new FileOutputStream(resource.getFile()),
				"Do not change or delete this file");
	}

	public Properties loadTechniqueHashesProperties(ResourceLoader resourceLoader)
			throws IOException {
		Properties properties = new Properties();
		properties.load(resourceLoader.getResource(this.hashFileLocation).getInputStream());

		return properties;
	}

}
