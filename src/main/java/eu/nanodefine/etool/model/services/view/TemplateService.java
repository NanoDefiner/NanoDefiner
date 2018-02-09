/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.view;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.FileService;

/**
 * Generic service for template-related processing.
 *
 * TODO make more specific and move methods?
 */
@Service("TS")
public class TemplateService implements IService {

	protected final ServiceManager serviceManager;

	@Autowired
	public TemplateService(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	/**
	 * Generates a string representation of the file size of the given file.
	 *
	 * <p>The file name is expected to be the name of a file in the analysis directory.</p>
	 *
	 * @see #generateFileSize(Path)
	 */
	public String generateFileSize(String fileName) {
		return this.generateFileSize(this.serviceManager.getBean(FileService.class)
				.getFullPathInAnalysisDirectory(fileName));
	}

	/**
	 * Generates a string representation of the file size of the file at the given path.
	 */
	public String generateFileSize(Path path) {
		File file = path.toFile();

		if (file.exists()) {
			return "[" + this.serviceManager.getBean(NumberService.class)
					.formatFileSize(file.length()) + "]";
		}

		return "";
	}

	/**
	 * Returns an empty list, for use in templates where access to static methods is difficult.
	 */
	public List<?> list() {
		return ImmutableList.of();
	}

	/**
	 * Creates a list from the given elements, for use in templates where access to static methods is
	 * difficult.
	 */
	public List<Object> list(Object... elements) {
		return Arrays.asList(elements);
	}

	public Map<String, String> map(String... elements) {
		if ((elements.length % 2) != 0) {
			throw new RuntimeException("Map creation requires even number of elements");
		}

		Map<String, String> map = new HashMap<>();

		for (int i = 0; i < elements.length; i += 2) {
			map.put(elements[i], elements[i + 1]);
		}

		return map;
	}
}
