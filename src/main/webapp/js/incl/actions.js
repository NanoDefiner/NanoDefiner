/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

/**
 * TODO documentation
 */
ND.actions = ND.actions || {};

ND.actions.postLoadMoveActionBox = function() {

	var actionBox = document.getElementById("actionsGlobal");
	var $pageHeaders = $('.page-header[data-classes!="no-actionbox"]');
	var $pageHeadersExplicit = $('.page-header[data-classes="actionbox"]');
	var pageHeader = null;

	if ($pageHeadersExplicit.length > 0) {
		pageHeader = $pageHeadersExplicit.get(0);
	} else if ($pageHeaders.length > 0) {
		pageHeader = $pageHeaders.get(0);
	}

	if ($(".actions").length === 1 && pageHeader !== null) {
		pageHeader.parentNode.insertBefore(actionBox, pageHeader.nextSibling);
		$("#actionsGlobal").removeClass("hidden");
	} else {
		$("#actionsGlobal").remove();
	}
};

$(document).ready(ND.actions.postLoadMoveActionBox);
