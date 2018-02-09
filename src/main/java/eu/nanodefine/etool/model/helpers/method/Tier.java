/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.method;

/**
 * Helper class for method tiers.
 *
 * <p>Usually, a tier is identified using a string, e.g. "tier1_na", meaning "tier 1, not
 * assessed". This class helps extracting the base tier (1 or 2) and whether it is assessed or not
 * from these string representations.</p>
 */
public class Tier {

	private boolean assessed;

	private String baseTier;

	private String tier;

	public Tier(String tier) {
		this.tier = tier;

		if (this.tier.contains("_na")) {
			this.assessed = false;
			this.baseTier = this.tier.substring(1, this.tier.length() - 4);
		} else {
			this.assessed = true;
			this.baseTier = this.tier.substring(1, this.tier.length() - 1);
		}
	}

	public String getBaseTier() {
		return this.baseTier;
	}

	public String getTier() {
		return this.tier;
	}

	public boolean isAssessed() {
		return this.assessed;
	}
}
