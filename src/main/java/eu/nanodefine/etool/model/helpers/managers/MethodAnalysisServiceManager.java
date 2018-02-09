/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nanodefine.etool.exception.runtime.NoSuchBeanException;
import eu.nanodefine.etool.model.services.method.analysis.IAnalysisDataFormatService;
import eu.nanodefine.etool.model.services.method.constants.AnalysisDataFormats;

/**
 * Bean manager for {@link IAnalysisDataFormatService}s.
 */
@Component
public class MethodAnalysisServiceManager implements IBeanTypeManager<IAnalysisDataFormatService> {

	private Map<Class<? extends IAnalysisDataFormatService>, IAnalysisDataFormatService> beanMap;

	private boolean initialized = false;

	@Autowired
	private List<IAnalysisDataFormatService> methodProcessors;

	/**
	 * Additional bean mapping using string keys (analysis data format).
	 */
	private Map<String, IAnalysisDataFormatService> processorMap;

	/**
	 * This manager builds the map of available beans when the first one is requested.
	 */
	private void buildMaps() {
		this.beanMap = new HashMap<>(this.getAvailableBeans().size());
		this.processorMap = new HashMap<>(this.getAvailableBeans().size());

		for (IAnalysisDataFormatService processor : this.getAvailableBeans()) {
			this.beanMap.put(processor.getClass(), processor);
			this.processorMap.put(processor.getAnalysisDataFormat(), processor);
		}

		this.initialized = true;
	}

	@Nonnull
	private String determineAnalysisDataFormat(@Nullable String analysisDataFormat) {
		return analysisDataFormat == null ? AnalysisDataFormats.UNKNOWN : analysisDataFormat;
	}

	@Override
	public List<IAnalysisDataFormatService> getAvailableBeans() {
		return this.methodProcessors;
	}

	@Nonnull
	@Override
	public <S extends IAnalysisDataFormatService> S getBean(Class<S> clazz) {
		assert clazz != null;

		// Throw exception if it still does not exist
		if (!this.getBeanMap().containsKey(clazz)) {
			throw new NoSuchBeanException("No bean found for class: " + clazz.getCanonicalName());
		}

		return (S) this.getBeanMap().get(clazz);
	}

	/**
	 * Returns an {@link IAnalysisDataFormatService} for the given analysis data format.
	 */
	@Nonnull
	public <S extends IAnalysisDataFormatService> S getBean(@Nullable String analysisDataFormat) {
		analysisDataFormat = this.determineAnalysisDataFormat(analysisDataFormat);

		// Throw exception if bean not found
		if (!this.hasBean(analysisDataFormat)) {
			throw new NoSuchBeanException("No analysis data processor for format: " + analysisDataFormat);
		}

		return (S) this.getProcessorMap().get(analysisDataFormat);
	}

	@Override
	public Map<Class<? extends IAnalysisDataFormatService>, IAnalysisDataFormatService> getBeanMap() {
		if (!this.initialized) {
			this.buildMaps();
		}

		return this.beanMap;
	}

	/**
	 * Returns the processor map, using analysis data format strings as keys.
	 */
	public Map<String, IAnalysisDataFormatService> getProcessorMap() {
		if (!this.initialized) {
			this.buildMaps();
		}

		return this.processorMap;
	}

	@Override
	public <S extends IAnalysisDataFormatService> boolean hasBean(Class<S> clazz) {
		assert clazz != null;
		return this.getBeanMap().containsKey(clazz);
	}

	public boolean hasBean(String analysisDataFormat) {
		analysisDataFormat = this.determineAnalysisDataFormat(analysisDataFormat);

		return this.getProcessorMap().containsKey(analysisDataFormat);
	}
}
