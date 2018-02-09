/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.drools;

import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.rule.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsEventListener extends DefaultAgendaEventListener {

	private Logger logger = null;

	public DroolsEventListener() {
		this.logger = LoggerFactory.getLogger(DroolsEventListener.class);
	}

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {
		super.afterMatchFired(event);

		Match match = event.getMatch();
		Rule rule = match.getRule();
/*
		logger.debug("");
		logger.debug("----------------------------------------"
				+ "----------------------------------------");
		logger.debug("--- RULE FIRED -------------------------"
				+ "----------------------------------------");
		logger.debug("----------------------------------------"
				+ "----------------------------------------");

		logger.trace("MATCH declaration id:value");
		for (String id : match.getDeclarationIds())
			logger.trace(id + ":" + match.getDeclarationValue(id));
		// Output: message:Goodbye cruel world

		logger.debug("MATCH fact handles");
		for (FactHandle fh : match.getFactHandles())
			logger.debug(fh.toExternalForm());
		// Output: 0:1:197840080:197840080:2:DEFAULT:NON_TRAIT:eu.nanodefine.etool.drools.Message

		logger.trace("MATCH objects");
		for (Object o : match.getObjects())
			logger.trace("" + o);
		// Output: eu.nanodefine.etool.drools.Message@bcaccd0

		logger.debug("RULE id : " + rule.getId());
		logger.debug("RULE name : " + rule.getName());
		logger.debug("RULE namespace : " + rule.getNamespace());
		logger.debug("RULE package name : " + rule.getPackageName());
		logger
				.debug("RULE knowledge type name : " + rule.getKnowledgeType().name());
				/*
				logger.debug("RULE meta data (key : value) :");
        for (String key : rule.getMetaData().keySet())
        	logger.debug(key + " : " + rule.getMetaData().get(key));
        */
		this.logger.debug("----------------------------------------"
				+ "----------------------------------------");
	}

}
