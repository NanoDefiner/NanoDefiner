/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.trim = ND.trim || {};

/**
 * Automatic trimming of non-password form element contents
 */
ND.trim.setupFormInputTrimming = function() {
	$("input[type!=password]").change(function () {
		$(this).val($(this).val().trim());
	});

	$("textarea").change(function () {
		$(this).text($(this).text().trim());
	});
};

$(document).ready(function () {
	ND.trim.setupFormInputTrimming();
});
