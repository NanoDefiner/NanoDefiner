/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.modals = ND.modals || {};

ND.modals.toggleModalShow = function() {
	var cookieId = $(this).attr('data-cookie');

	Cookies.set(cookieId, $(this).prop('checked'), {expires: 365});
};

ND.modals.skipModal = function(id) {
	// Find button or link which triggers the modal
	var $modalTrigger = $("[data-target^='#" + id + "']");
	// And register new handler which directly redirects to the target page
	$modalTrigger.click(function () {
		var linkId = $(this).attr('data-target');
		var target = $(linkId + ' .btn-primary').attr('href');
		window.location.href = target;
		return false;
	});
};

ND.modals.setupModalCookies = function() {
	var $modalInputs = $('div.modal input[data-cookie]');

	var modalGroups = [];

	$modalInputs.each(function () {
		var cookieId = $(this).attr('data-cookie');

		// Only do this once for each cookie ID
		if (modalGroups.indexOf(cookieId) >= 0) {
			return;
		}

		var cookieValue = Cookies.get(cookieId);

		// TODO undefined check needed?
		if (typeof cookieValue !== 'undefined' && cookieValue === "true") {
			ND.modals.skipModal(cookieId);
		} else {
			$(this).prop('checked', false);
		}

		modalGroups.push(cookieId);
	});

	$modalInputs.change(ND.modals.toggleModalShow);
};

/**
 * Function for moving all modals within the DOM tree to avoid graphical
 * artifacts or wrongly styled modals when inserting them within other elements.
 */
ND.modals.postLoadMoveModals = function() {
	$(".modal").each(function () {
		document.getElementById('modalBox').appendChild(this);
	});
	document.getElementsByTagName('body')[0].appendChild(document.getElementById('modalBox'));
};

$(document).ready(ND.modals.postLoadMoveModals);
$(document).ready(ND.modals.setupModalCookies);
