/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads MCS help images.
 */
@Configuration
public class ImageConfig {

	private Logger log = LoggerFactory.getLogger(ImageConfig.class);

	/**
	 * Map of help images for the MCS.
	 */
	@Bean
	@Autowired
	public Map<String, String> mcsImages(ServletContext servletContext)
			throws IOException, URISyntaxException {

		String folder = "/img/mcs/"; // TODO extract into constant / configuration
		Map<String, String> imageMap = new HashMap<>();
		Set<String> imagePaths = servletContext.getResourcePaths(folder);

		if (imagePaths == null) {
			this.log.warn("Could not list files in MCS image directory");
			return imageMap;
		}

		String[] imageFilenameParts;
		String fileName;
		for (String path : imagePaths) {
			fileName = path.replaceFirst(folder, "");
			imageFilenameParts = fileName.split("\\.");

			// File name is attribute.value.ext, so we need at least three parts
			if (imageFilenameParts.length < 3) {
				this.log.warn("Invalid MCS image file name: {}", fileName);
				continue;
			}

			this.log.debug("Adding MCS image: {} -> {}",
					imageFilenameParts[0] + "." + imageFilenameParts[1], fileName);

			imageMap.put(imageFilenameParts[0] + "." + imageFilenameParts[1], fileName);
		}

		return imageMap;
	}

}
