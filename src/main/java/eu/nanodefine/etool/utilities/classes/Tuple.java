/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.utilities.classes;

/**
 * Simple generic tuple class for element pairs.
 *
 * @param <L>
 *            type of left element
 * @param <R>
 *            type of right element
 */
public class Tuple<L, R> {

	private L left;

	private R right;

	public Tuple() {
		this.left = null;
		this.right = null;
	}

	public Tuple(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return this.left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public void setRight(R right) {
		this.right = right;
	}

	public R getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() ^ this.right.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Tuple)) {
			return false;
		}

		Tuple t = (Tuple) o;
		return this.left.equals(t.getLeft()) && this.right.equals(t.getRight());
	}

}
