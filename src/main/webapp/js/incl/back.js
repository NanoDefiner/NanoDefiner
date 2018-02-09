/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.back = ND.back || {};

/**
 * Automatically inserts a back link into the action box
 */
ND.back.insertBackLink = function() {
	// Abort if no action box
	if ($("div.actions").length === 0) return;

	// TODO locales
	var linkText = window.history.length > 1 ?
			ND.util.getLocaleString("global.action.back.previous_page") :
			ND.util.getLocaleString("global.action.back.home_page");

	var $li = $('<li><div><a href="#" id="backLink"><span class="glyphicon glyphicon-menu-right"></span>' + "\n" + '<b>'
			+ ND.util.getLocaleString("global.action.back.label") + '</b></a> &mdash; <span>'
			+ linkText + '</span></div></li>');

	$("div.actions ul").append($li);

	$("#backLink").click(ND.back.goBack);
};

/**
 * If there's a back button somewhere on the page, automatically set it up
 */
ND.back.setupBackButton = function() {

	var $backButton = $("#backButton");

	if ($backButton.length === 0) return;

	$backButton.click(ND.back.goBack);
};

ND.back.goBack = function() {

	if (window.history.length > 1) {
		window.history.back();
	} else {
		window.location.href = $("a.navbar-brand").attr('href');
	}

	return false;
};

$(document).ready(ND.back.insertBackLink);
$(document).ready(ND.back.setupBackButton);
