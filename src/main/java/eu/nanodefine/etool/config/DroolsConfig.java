/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.config;

import java.util.List;
import java.util.Vector;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Drools configuration.
 */
@Configuration
public class DroolsConfig {

	private Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

	/**
	 * Configures Drools and provides the {@link KieContainer} bean.
	 */
	@Bean
	public KieContainer kieContainer() {

		this.logger.info("Starting drools configuration");

		// Get KIE service and create a file system for rule files
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFS = kieServices.newKieFileSystem();

		/*
		 * TODO Use configured rule files
		 *
		 * // Get config URL URL configURL = null; try { String configLocation =
		 * getServletConfig().getInitParameter(ConfigAttributes.CONFIG);
		 * configURL = getServletContext().getResource(configLocation); } catch
		 * (MalformedURLException e) {
		 * logger.error("Malformed configuration path", e); throw new
		 * ServletException(e); }
		 */

		// TODO Read from config
		// Read rule files, write them to die KIE file system and build 'em
		List<String> drls = new Vector<>();
		drls.add("drools/monoconst.drl");
		drls.add("drools/monoconst_expl.drl");
		// drls.add("drools/monoconst_preproc.drl");
		// drls.add("drools/multiconst.drl");
		// drls.add("drools/multiconst_preproc.drl");
		for (String drl : drls) {
			kieFS.write(ResourceFactory.newClassPathResource(drl));
		}
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFS).buildAll();

		// Check build results
		Results results = kieBuilder.getResults();
		if (results.hasMessages(Level.WARNING, Level.ERROR)) {
			for (Message m : results.getMessages(Level.WARNING)) {
				this.logger.warn(m.getText());
			}
			for (Message m : results.getMessages(Level.ERROR)) {
				this.logger.error(m.getText());
			}

			if (results.hasMessages(Level.ERROR)) {
				throw new IllegalStateException(
						"there were errors in the drools KIE building process");
			}
		}

		// Create default KIE container and store it in the servlet context
		ReleaseId id = kieServices.getRepository().getDefaultReleaseId();
		KieContainer kieContainer = kieServices.newKieContainer(id);

		this.logger.info("Finished drools configuration");

		return kieContainer;
	}

}
