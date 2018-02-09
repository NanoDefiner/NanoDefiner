/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.helpers.technique;

import javax.annotation.Nullable;

/**
 * Class containing the validated and current KB hashes for a specific technique configuration.
 */
public class HashResult {

	private String hashCurrent;

	private String hashValidated;

	public HashResult(@Nullable String hashValidated, String hashCurrent) {
		this.hashValidated = hashValidated;
		this.hashCurrent = hashCurrent;
	}

	public String getHashCurrent() {
		return this.hashCurrent;
	}

	@Nullable
	public String getHashValidated() {
		return this.hashValidated;
	}

	public boolean hasValidatedHash() {
		return this.hashValidated != null;
	}

	/**
	 * Returns whether the current hash is considered invalid.
	 *
	 * <p>The current hash is considered invalid if there is a validated hash present and the current
	 * and validated hashes are not the same.</p>
	 */
	public boolean isInvalid() {
		return this.hashValidated != null && !this.hashValidated.equals(this.hashCurrent);
	}
}
