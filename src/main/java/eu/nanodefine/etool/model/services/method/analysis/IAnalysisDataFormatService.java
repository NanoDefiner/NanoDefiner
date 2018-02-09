/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.method.analysis;

import java.io.IOException;

import eu.nanodefine.etool.model.dto.Method;
import eu.nanodefine.etool.view.report.DynamicPdfReport;

/**
 * Interface for services handling method analysis data.
 */
public interface IAnalysisDataFormatService {

	/**
	 * Adds method results to a report.
	 */
	void addMethodResults(Method method, DynamicPdfReport report) throws IOException;

	/**
	 * Determine the method state based on the method's attribute.
	 *
	 * <p>Possible states are documented in
	 * {@link eu.nanodefine.etool.model.services.method.constants.MethodStates}.</p>
	 *
	 * @see eu.nanodefine.etool.model.services.method.constants.MethodStates
	 */
	Integer determineState(Method method);

	/**
	 * Returns the analysis data format handled by this service.
	 */
	String getAnalysisDataFormat();

	/**
	 * Updates the method with initial analysis data and an uncertainty measure.
	 *
	 * <p>This usually involves processing the result using an
	 * {@link eu.nanodefine.etool.analysis.processors.IAnalysisProcessor} and then registering
	 * the result using {@link eu.nanodefine.etool.model.services.method.MethodService#registerResult(Method, Double)}.</p>
	 *
	 */
	void update(Method method, String data, Double uncertainty) throws Exception;

	/**
	 * Updates the method with further information (e.g. size plausibility check)
	 */
	void update(Method method, String data) throws Exception;

}
