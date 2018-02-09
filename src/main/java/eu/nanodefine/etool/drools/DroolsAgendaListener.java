/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.drools;

import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO This is just a backup, check it!
 */
public class DroolsAgendaListener extends DefaultAgendaEventListener {

	private Logger log = LoggerFactory
			.getLogger(DefaultAgendaEventListener.class);

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {

		super.afterMatchFired(event);

		Match match = event.getMatch();
		Rule rule = match.getRule();

		this.log.trace("--- RULE FIRED -------------------------"
				+ "----------------------------------------");

		this.log.trace("MATCH declaration id:value");
		for (String id : match.getDeclarationIds()) {
			this.log.trace(id + ":" + match.getDeclarationValue(id));
		}
		// Output: message:Goodbye cruel world

		this.log.trace("MATCH fact handles");
		for (FactHandle fh : match.getFactHandles()) {
			this.log.trace(fh.toExternalForm());
		}
		// Output: 0:1:197840080:197840080:2:DEFAULT:NON_TRAIT:eu.nanodefine.etool.drools.Message

		this.log.trace("MATCH objects");
		for (Object o : match.getObjects()) {
			this.log.trace("" + o);
		}
		// Output: eu.nanodefine.etool.drools.Message@bcaccd0

		this.log.trace("RULE id : " + rule.getId());
		this.log.trace("RULE name : " + rule.getName());
		this.log.trace("RULE namespace : " + rule.getNamespace());
		this.log.trace("RULE package name : " + rule.getPackageName());
		this.log.trace("RULE knowledge type name : "
				+ rule.getKnowledgeType().name());
		/*
		this.log.trace("RULE meta data (key : value) :");
		for (String key : rule.getMetaData().keySet())
			this.log.trace(key + " : " + rule.getMetaData().get(key));
		*/
		this.log.trace("----------------------------------------"
				+ "----------------------------------------");
	}
}
