/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.pace = ND.pace || {};

/**
 * Show hidden elements after pace is done.
 */

ND.pace.postPaceShowElements = function() {
	// Adjust position of loading bar
	var heightNavbar = $('nav').outerHeight();

	$paceHidden = $('.pace-hidden');
	$paceHidden.attr('data-classes', 'pace-hidden');
	$paceHidden.addClass('hidden');
	$paceHidden.removeClass('pace-hidden');

	Pace.on('start', function () {
		$(".pace .pace-progress").css("top", heightNavbar + "px");
	});

	Pace.on('done', ND.pace.postPaceDoneRemoveHidden);

	// TODO solve cleaner
	$(".pace .pace-progress").css("top", heightNavbar + "px");
};

ND.pace.postPaceDoneRemoveHidden = function() {
	$('[data-classes~=pace-hidden]').removeClass('hidden');
};

$(document).ready(ND.pace.postPaceShowElements);
