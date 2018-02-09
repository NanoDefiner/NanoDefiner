/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.user = ND.user || {};
ND.user.update = ND.user.update || {};

/**
 * TODO documentation
 */

ND.user.update.setupEnableCheckboxes = function() {
	$("input[name=enabled]").each(function() {
		var $tr = $(this).closest("tr");
		var $hiddenSpan = $tr.find("span[data-classes=enabled]");
		$(this).prop("checked", $hiddenSpan.text() === "true");
		$(this).attr("value", "1");
	});

	$("button[name=submit]").click(ND.user.update.preForumSubmitCheckboxUpdate);
};

ND.user.update.preForumSubmitCheckboxUpdate = function() {
	$("input[name=enabled]").each(function() {
		var $tr = $(this).closest("tr");
		var $hiddenSpan = $tr.find("span[data-classes=enabled]");
		$(this).attr("name", $hiddenSpan.attr("name"));
	});
};

$(document).ready(ND.user.update.setupEnableCheckboxes);
