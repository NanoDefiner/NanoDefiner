/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import eu.nanodefine.etool.constants.PerformanceAttributes;
import eu.nanodefine.etool.exception.runtime.ConfigurationException;
import eu.nanodefine.etool.knowledge.configurations.ExplanationConfiguration;
import eu.nanodefine.etool.knowledge.configurations.MaterialConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PerformanceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.PriorityConfiguration;
import eu.nanodefine.etool.knowledge.configurations.ReferenceConfiguration;
import eu.nanodefine.etool.knowledge.configurations.TechniqueConfiguration;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredMaterial;
import eu.nanodefine.etool.knowledge.configurations.beans.ConfiguredPerformance;
import eu.nanodefine.etool.knowledge.dictionaries.ExplanationDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.MaterialDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PerformanceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.PriorityDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.ReferenceDictionary;
import eu.nanodefine.etool.knowledge.dictionaries.TechniqueDictionary;
import eu.nanodefine.etool.model.dto.Technique;
import eu.nanodefine.etool.model.helpers.technique.HashResult;
import eu.nanodefine.etool.model.repositories.TechniqueRepository;
import eu.nanodefine.etool.model.services.ConfigService;
import eu.nanodefine.etool.model.services.FileService;
import eu.nanodefine.etool.model.services.technique.TechniqueHashService;
import eu.nanodefine.etool.utilities.utils.ConfigurationUtil;

/**
 * Spring configuration class for the knowledge base.
 */
@Configuration
public class KnowledgeConfig {

	/**
	 * Location of the reference configuration.
	 */
	@Value("${knowledge.explanation.config}")
	private String explanationConfLocation;

	/**
	 * Location of the reference dictionary.
	 */
	@Value("${knowledge.explanation.dict}")
	private String explanationDictLocation;

	/**
	 * Location of the file containing the technique hashes.
	 */
	@Value("${knowledge.hashes}")
	private String hashFileLocation;

	private Logger log = LoggerFactory.getLogger(KnowledgeConfig.class);

	/**
	 * Location of the material configuration.
	 */
	@Value("${knowledge.material.config}")
	private String materialConfLocation;

	/**
	 * Location of the material dictionary.
	 */
	@Value("${knowledge.material.dict}")
	private String materialDictLocation;

	/**
	 * Location of the performance configuration.
	 */
	@Value("${knowledge.performance.config}")
	private String performanceConfLocation;

	/**
	 * Location of the performance dictionary.
	 */
	@Value("${knowledge.performance.dict}")
	private String performanceDictLocation;

	/**
	 * Location of the priority configuration.
	 */
	@Value("${knowledge.priority.config}")
	private String priorityConfLocation;

	/**
	 * Location of the priority dictionary.
	 */
	@Value("${knowledge.priority.dict}")
	private String priorityDictLocation;

	/**
	 * Production/debug mode switch.
	 */
	@Value("${dev.production}")
	private boolean production;

	/**
	 * Location of the reference configuration.
	 */
	@Value("${knowledge.reference.config}")
	private String referenceConfLocation;

	/**
	 * Location of the reference dictionary.
	 */
	@Value("${knowledge.reference.dict}")
	private String referenceDictLocation;

	/**
	 * Location of the technique configuration.
	 */
	@Value("${knowledge.technique.config}")
	private String techniqueConfLocation;

	/**
	 * Location of the technique dictionary.
	 */
	@Value("${knowledge.technique.dict}")
	private String techniqueDictLocation;

	@Autowired
	private TechniqueHashService techniqueHashService;


	/**
	 * Provides the explanation configuration bean.
	 */
	@Bean
	@Autowired
	public ExplanationConfiguration explanationConfiguration(
			ResourceLoader resourceLoader) throws IOException {
		return new ExplanationConfiguration(
				this.getResourceInputStream(this.explanationConfLocation,
						"explanation configuration", resourceLoader));
	}

	/**
	 * Provides the explanation dictionary bean.
	 */
	@Bean
	@Autowired
	public ExplanationDictionary explanationDictionary(
			ResourceLoader resourceLoader) {
		return new ExplanationDictionary(
				this.getResourceInputStream(this.explanationDictLocation,
						"explanation dictionary", resourceLoader));
	}

	/**
	 * Helper method to load various critical resources.
	 *
	 * Being unable to load a resource will result in the immediate termination
	 * of the program.
	 *
	 * TODO extract into utility class?
	 */
	private InputStream getResourceInputStream(String location, String name,
			ResourceLoader resourceLoader) {

		this.log.debug("{} location: {}", name, location);
		InputStream inputStream;
		try {
			inputStream = resourceLoader.getResource(location).getInputStream();
		} catch (IOException e) {
			this.log.error("unable to load resource {}: {}", name, e);
			throw new ConfigurationException("Could not load required resource");
		}

		return inputStream;
	}

	/**
	 * Creates an archive of the knowledge base in the analysis directory.
	 *
	 * @return Full path to the knowledge base archive.
	 */
	@Autowired
	@Bean
	public Path knowledgeBaseArchivePath(FileService fileService,
			ResourceLoader resourceLoader) throws IOException {
		Map<String, String> env = ImmutableMap.of("create", "true");

		// TODO extract archive name to constant
		Path zipArchive = fileService.getFullPathInAnalysisDirectory("KB.zip");
		Files.deleteIfExists(zipArchive);

		URI uri = URI.create("jar:file:" + zipArchive.toString());
		List<Object> archiveEntries = ImmutableList.builder().add(this.explanationConfLocation,
				this.explanationDictLocation, this.materialConfLocation, this.materialDictLocation,
				this.performanceConfLocation, this.performanceDictLocation, this.priorityConfLocation,
				this.performanceDictLocation, this.referenceConfLocation, this.referenceDictLocation,
				this.techniqueConfLocation, this.techniqueDictLocation).build();

		Path pathInZipfile;
		Resource resourceExternal;
		try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {

			for (Object fileLocation : archiveEntries) {
				resourceExternal = resourceLoader.getResource(fileLocation.toString());
				pathInZipfile = zipfs.getPath(resourceExternal.getFilename());

				// copy a file into the zip file
				Files.copy(resourceExternal.getInputStream(), pathInZipfile,
						StandardCopyOption.REPLACE_EXISTING);
			}
		}

		return zipArchive;
	}

	/**
	 * Provides the material configuration bean.
	 */
	@Bean
	@Autowired
	public MaterialConfiguration materialConfiguration(ResourceLoader resourceLoader)
			throws IOException {

		return new MaterialConfiguration(this.getResourceInputStream(this.materialConfLocation,
				"material configuration", resourceLoader));
	}

	/**
	 * Provides the material dictionary bean.
	 */
	@Bean
	@Autowired
	public MaterialDictionary materialDictionary(ResourceLoader resourceLoader) {

		return new MaterialDictionary(
				this.getResourceInputStream(this.materialDictLocation,
						"material dictionary", resourceLoader));
	}

	/**
	 * Filters the configured materials returning only material groups.
	 */
	@Bean
	@Autowired
	public List<ConfiguredMaterial> materialGroups(MaterialConfiguration materialConfiguration) {

		return materialConfiguration.getEntries().stream().filter(
				e -> e.getGroup().equals(ConfigurationUtil.BINARY_YES)).collect(Collectors.toList());
	}

	/**
	 * Filters the configured materials returning only material types.
	 */
	@Bean
	@Autowired
	public List<ConfiguredMaterial> materialTypes(MaterialConfiguration materialConfiguration) {

		return materialConfiguration.getEntries().stream().filter(
				e -> e.getGroup().equals(ConfigurationUtil.BINARY_NO)).collect(Collectors.toList());
	}

	/**
	 * Provides the performance configuration bean.
	 */
	@Bean
	@Autowired
	public PerformanceConfiguration performanceConfiguration(ResourceLoader resourceLoader)
			throws IOException {

		return new PerformanceConfiguration(
				this.getResourceInputStream(this.performanceConfLocation,
						"performance configuration", resourceLoader));
	}

	/**
	 * Provides the performance dictionary bean.
	 */
	@Bean
	@Autowired
	public PerformanceDictionary performanceDictionary(ResourceLoader resourceLoader) {

		return new PerformanceDictionary(this.getResourceInputStream(this.performanceDictLocation,
				"performance dictionary", resourceLoader));
	}

	/**
	 * Provides the priority configuration bean.
	 */
	@Bean
	@Autowired
	public PriorityConfiguration priorityConfiguration(ResourceLoader resourceLoader)
			throws IOException {

		return new PriorityConfiguration(this.getResourceInputStream(this.priorityConfLocation,
				"priority configuration", resourceLoader));
	}

	/**
	 * Provides the priority dictionary bean.
	 */
	@Bean
	@Autowired
	public PriorityDictionary priorityDictionary(ResourceLoader resourceLoader) {

		return new PriorityDictionary(this.getResourceInputStream(this.priorityDictLocation,
				"priority dictionary", resourceLoader));
	}

	/**
	 * Provides the reference configuration bean.
	 */
	@Bean
	@Autowired
	public ReferenceConfiguration referenceConfiguration(ResourceLoader resourceLoader)
			throws IOException {

		return new ReferenceConfiguration(this.getResourceInputStream(this.referenceConfLocation,
				"reference configuration", resourceLoader));
	}

	/**
	 * Provides the reference dictionary bean.
	 */
	@Bean
	@Autowired
	public ReferenceDictionary referenceDictionary(
			ResourceLoader resourceLoader) {

		return new ReferenceDictionary(this.getResourceInputStream(this.referenceDictLocation,
				"reference dictionary", resourceLoader));
	}

	/**
	 * Provides the technique configuration bean.
	 */
	@Bean
	@Autowired
	public TechniqueConfiguration techniqueConfiguration(
			ResourceLoader resourceLoader) throws IOException {

		return new TechniqueConfiguration(this.getResourceInputStream(this.techniqueConfLocation,
				"technique configuration", resourceLoader));
	}

	/**
	 * Provides the technique dictionary bean.
	 */
	@Bean
	@Autowired
	public TechniqueDictionary techniqueDictionary(
			ResourceLoader resourceLoader) {

		return new TechniqueDictionary(this.getResourceInputStream(this.techniqueDictLocation,
				"technique dictionary", resourceLoader));
	}

	/**
	 * Creates a table of technique hashes.
	 *
	 * <p>Each row containts the technique, the validated hash (may be null) and the KB hash.</p>
	 *
	 * @see TechniqueHashService#createTechniqueHash(ConfiguredPerformance, PerformanceDictionary)
	 * @see TechniqueHashService#createTechniqueHashesFile(List, Resource, PerformanceConfiguration, PerformanceDictionary)
	 */
	@Bean
	@Autowired
	public Table<String, String, HashResult> techniqueHashes(
			@Qualifier("techniques") List<Technique> techniques,
			PerformanceConfiguration performanceConfiguration,
			PerformanceDictionary performanceDictionary, ResourceLoader resourceLoader)
			throws IOException, NoSuchAlgorithmException {

		Resource resource = resourceLoader.getResource(this.hashFileLocation);

		if (!resource.exists()) {
			if (this.production) {
				throw new ConfigurationException("Technique hashes file not found");
			}

			this.techniqueHashService
					.createTechniqueHashesFile(techniques, resource, performanceConfiguration,
							performanceDictionary);
		}

		Properties properties = this.techniqueHashService.loadTechniqueHashesProperties(resourceLoader);

		Table<String, String, HashResult> techniqueHashes = HashBasedTable
				.create(techniques.size(), performanceDictionary.getAttributes().size());

		String hashValidated, hashCurrent, materialSignifier, propertyKey;
		HashResult hashResult;
		for (Technique technique : techniques) {
			for (ConfiguredPerformance cp : performanceConfiguration
					.getEntriesForTechniqueSignifier(technique.getSignifier())) {

				materialSignifier = cp.getEntry(PerformanceAttributes.MATERIAL_SIGNIFIER).getValue();
				propertyKey = this.techniqueHashService.buildPropertyKey(technique.getSignifier(),
						materialSignifier);
				hashValidated = properties.getProperty(propertyKey);
				hashCurrent = this.techniqueHashService.createTechniqueHash(cp, performanceDictionary);
				hashResult = new HashResult(hashValidated, hashCurrent);

				techniqueHashes.put(technique.getSignifier(), materialSignifier, hashResult);

				this.log.debug("Technique hash check: {}: {}/{}", propertyKey,
						hashResult.isInvalid(), hashResult.hasValidatedHash());
			}
		}

		return techniqueHashes;

	}

	/**
	 * Loads all available techniques from the database.
	 *
	 * If the database does not contain any techniques, the initial set of
	 * techniques is written to the database first.
	 */
	@Bean
	@Autowired
	public List<Technique> techniques(TechniqueConfiguration techniqueConfiguration,
			TechniqueRepository techniqueRepository, ConfigService configService) {

		return configService.updateTechniques(techniqueConfiguration.getEntries());
	}
}
