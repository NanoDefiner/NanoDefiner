/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import eu.nanodefine.etool.model.dto.CustomAttribute;
import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.ICustomAttributeEntity;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.repositories.CustomAttributeRepository;
import eu.nanodefine.etool.model.services.report.ReportService;

/**
 * Service for custom attribute-related processing.
 */
@Service
@Transactional
public class CustomAttributeService implements IService {

	private final CustomAttributeRepository customAttributeRepository;

	private final ServiceManager serviceManager;

	private Logger log = LoggerFactory.getLogger(CustomAttributeService.class);

	@Autowired
	public CustomAttributeService(ServiceManager serviceManager,
			CustomAttributeRepository customAttributeRepository) {
		this.customAttributeRepository = customAttributeRepository;
		this.serviceManager = serviceManager;
	}

	/**
	 * Deletes the file associated with the given custom attribute if it exists.
	 */
	private void deleteCustomAttributeFileIfExists(CustomAttribute customAttribute) {
		if (customAttribute.isFile()) {
			try {
				Files.deleteIfExists(Paths.get(customAttribute.getValue()));
			} catch (IOException e) {
				this.log.error("Error trying to delete custom attribute file '{}': {}",
						customAttribute.getValue(), e.getMessage());
			}
		}
	}

	/**
	 * Deletes the custom attributes with the given IDs of the given parent entity.
	 *
	 * <p>The caller should validate the user's permission to change the parent entity; the
	 * custom attribute IDs do not have to be validated.</p>
	 */
	public void deleteCustomAttributesByIds(ICustomAttributeEntity parentEntity,
			Integer[] customAttributeIds) {
		Iterable<CustomAttribute> attributesToBeDeleted =
				this.customAttributeRepository.findAll(Arrays.asList(customAttributeIds));

		Set<CustomAttribute> customAttributes = parentEntity.getCustomAttributes();

		attributesToBeDeleted.forEach(customAttributes::remove);
		this.serviceManager.getBean(ReportService.class)
				.archiveCustomAttributeEntityReports(parentEntity);
	}

	/**
	 * Persists a custom attribute.
	 */
	public CustomAttribute persistCustomAttribute(ICustomAttributeEntity entity,
			CustomAttribute attribute, User user, String valueText, MultipartFile valueFile,
			List<String> errors) {
		assert valueFile != null || valueText != null;

		if (valueFile != null) {
			this.updateCustomAttributeFile(user, entity, valueFile, attribute, errors);
		} else {
			// TODO value sanitizing?
			attribute.setValue(valueText);
		}

		CustomAttribute attributePersisted = attribute;

		if (errors.isEmpty()) {
			attributePersisted = this.customAttributeRepository.save(attribute);
			entity.getCustomAttributes().add(attributePersisted);
			this.serviceManager.getBean(ReportService.class).archiveCustomAttributeEntityReports(entity);
		}

		return attributePersisted;
	}

	/**
	 * Updates the given persisted custom attribute with new data.
	 */
	public CustomAttribute updateCustomAttribute(ICustomAttributeEntity entity,
			CustomAttribute attributeForm, CustomAttribute attributePersisted, User user,
			String valueText, MultipartFile valueFile,
			List<String> errors) {

		if (valueText != null) {
			this.deleteCustomAttributeFileIfExists(attributePersisted);
			attributePersisted.setFile(false);
			attributePersisted.setValue(valueText);
		} else if (valueFile != null && !valueFile.isEmpty()) {
			this.updateCustomAttributeFile(user, entity, valueFile, attributePersisted, errors);
		} else if (!attributePersisted.isFile()) {
			throw new RuntimeException("Invalid custom attribute state: no value");
		}

		attributePersisted.setComment(attributeForm.getComment());
		attributePersisted.setName(attributeForm.getName());

		this.serviceManager.getBean(ReportService.class).archiveCustomAttributeEntityReports(entity);

		return attributePersisted;
	}

	/**
	 * Updates the file of the given custom attribute.
	 */
	private void updateCustomAttributeFile(User user, ICustomAttributeEntity entity,
			MultipartFile valueFile, CustomAttribute attribute, List<String> errors) {

		FileService fs = this.serviceManager.getBean(FileService.class);

		Path filePath = Paths.get(fs.buildFileNamePrefix(user, entity)
				+ valueFile.getOriginalFilename());
		Path oldFilePath = attribute.isFile()
				? fs.getFullPathInAnalysisDirectory(attribute.getValue()) : null;

		attribute.setFile(true);
		attribute.setValue(filePath.getFileName().toString());

		try {
			Files.write(filePath, valueFile.getBytes(), StandardOpenOption.CREATE_NEW);

			if (oldFilePath != null) {
				Files.deleteIfExists(oldFilePath);
			}
		} catch (IOException e) {
			this.log.error("Unable to write custom attribute file {}: {}", filePath.toString(),
					e.getMessage());
			errors.add("custom_attribute.create.error.file");
		}
	}
}
