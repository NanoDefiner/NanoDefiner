/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.pace = ND.pace || {};

/**
 * Show hidden elements after pace is done.
 */
ND.pace.postPaceShowElements = function () {
	$paceHidden = $('.pace-hidden');
	$paceHidden.attr('data-classes', 'pace-hidden');
	$paceHidden.addClass('hidden');
	$paceHidden.removeClass('pace-hidden');

	Pace.on('done', ND.pace.postPaceDoneRemoveHidden);
};

ND.pace.postPaceDoneRemoveHidden = function () {
	$('[data-classes~=pace-hidden]').removeClass('hidden');
};

$(document).ready(ND.pace.postPaceShowElements);
