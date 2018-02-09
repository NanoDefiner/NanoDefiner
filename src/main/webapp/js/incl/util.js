/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.util = ND.util || {};

/**
 * Removes an element from an {@link Array}.
 *
 * If the entry is not contained in the array, the array is returned
 * unchanged.
 *
 * @param options {@link Array} of options.
 * @param option Option to be removed.
 */
ND.util.removeElement = function(array, element) {
	var index = array.indexOf(element);

	if (index >= 0) {
		array.splice(index, 1);
	}

	return array;
};

/**
 * Returns the current time, rounded to 1 second.
 *
 * TODO insert workaround for missing Date.now in older IE versions?
 */
ND.util.getTime = function() {
	return Math.floor(Date.now());
};

/**
 * Returns the new element in an array.
 *
 * Like getNewElements, but it only returns the first new element.
 */
ND.util.getNewElement = function (arrayOld, arrayNew) {
	var newElements = ND.util.getNewElements(arrayOld, arrayNew);

	return newElements[0];
};

/**
 * Returns the new elements in an array.
 */
ND.util.getNewElements = function (arrayOld, arrayNew) {
	var newElements = [];

	if (typeof arrayOld === 'undefined') return [arrayNew[0]];

	for (var element in arrayNew) {
		if (arrayOld.indexOf(arrayNew[element]) === -1) {
			newElements.push(arrayNew[element]);
		}
	}

	return newElements;
};

ND.util.getLocaleString = function (id) {
	return $("#locale-" + id.replace(/\./g, "\\.")).text();
};

/**
 * https://gist.github.com/jakubp/2881585
 */
(function ($) {

	$.fn.removeClassesWithPrefix = function (prefix) {
		$.each(this, function (i, it) {
			var classes = it.className.split(" ").map(function (item) {
				return item.indexOf(prefix) === 0 ? "" : item;
			});
			it.className = $.trim(classes.join(" "));
		});

		return this;
	}

})(jQuery);
