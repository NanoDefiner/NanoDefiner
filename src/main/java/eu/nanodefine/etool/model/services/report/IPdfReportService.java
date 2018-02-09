/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.report;

import java.util.List;

import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.model.dto.Report;
import eu.nanodefine.etool.model.interfaces.IService;

/**
 * Interface for report-creating services.
 */
public interface IPdfReportService<T> extends IService {

	/**
	 * Create and write PDF report for the given report and methods.
	 */
	public T createAndWritePdfReport(Report report, List<Method> methods)
			throws Exception;

}
