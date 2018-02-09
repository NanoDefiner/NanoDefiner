/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.util = ND.util || {};

// TODO move functions to other files
ND.util.beforeUnload = function () {
	// Disable showing of elements, can lead to ugly effect
	Pace.off('done', ND.pace.postPaceDoneRemoveHidden);
	// Hide modals unless they are unloading modals
	$(".modal").not("[data-classes~=unloading-modal]").modal('hide');
	Pace.restart();
};

ND.util.setupFormsWithSubmitPopup = function () {
	$("form[data-classes~=submit-popup]").submit(function () {
		$("#" + $(this).attr('id') + "SubmitModal").modal('show');
	});
};

$(document).ready(ND.util.setupFormsWithSubmitPopup);
$(window).bind('beforeunload', ND.util.beforeUnload);
