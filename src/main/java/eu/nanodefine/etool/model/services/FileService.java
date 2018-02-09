/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.nanodefine.etool.model.dto.User;
import eu.nanodefine.etool.model.interfaces.IDataTransferObject;
import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Service for file-related processing.
 */
@Service("FS")
public class FileService implements IService {

	/**
	 * Date formatting in file names.
	 */
	private final String FILE_NAME_DATE_FORMAT = "yyyyMMddHHmmss";

	@Value("${server.data_directory}")
	private String analysisFileDirectory;

	/**
	 * Builds a file name prefix for the given user and entity.
	 *
	 * <p>The resulting file name will be {@code /path/to/analysis/directory/$userId.$entityType.$entityId.$timestamp_}</p>
	 */
	public String buildFileNamePrefix(User user, IDataTransferObject entity) {
		String timestamp = this.formatDateForFileName(new Date());

		return this.analysisFileDirectory + File.separator + user.getId() + '.'
				+ entity.getEntityType() + '.' + entity.getId() + '.' + timestamp + '_';
	}

	/**
	 * Extracts the original file name from the given path.
	 *
	 * <p>File names are expected to be made up two parts split by underscores. Everything after the
	 * underscore is the original file name.</p>
	 *
	 * @see #buildFileNamePrefix(User, IDataTransferObject)
	 */
	public String extractOriginalFileName(String filePath) {
		// TODO remove path stuff when all files are fixed
		return Paths.get(filePath).getFileName().toString().split("_", 2)[1];
		//return filePath.split("_", 2)[1];
	}

	/**
	 * Formats the given date to be used in a file name.
	 */
	public String formatDateForFileName(Date date) {
		return DateFormatUtils.format(date, this.FILE_NAME_DATE_FORMAT);
	}

	/**
	 * Returns the full path of a file in the analysis directory.
	 */
	public Path getFullPathInAnalysisDirectory(String fileName) {
		// For backwards-compatibility
		fileName = Paths.get(fileName).getFileName().toString();
		return Paths.get(this.analysisFileDirectory, fileName);
	}

}
