/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.messages = ND.messages || {};

ND.messages.setupMessages = function () {
	var nav = $("nav").get(0);
	var messageBox = document.getElementById("messages_alert");

	if (messageBox !== null) {
		nav.parentNode.insertBefore(messageBox, nav.nextSibling);
		$("#messages_alert").removeClass("hidden");
	}

	$('.alert[data-classes~=alert-message]').click(ND.messages.closeAlert);
};

ND.messages.closeAlert = function () {
	$(this).alert('close');
};

$(document).ready(ND.messages.setupMessages);
