/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

$(document).ready(function() {
	// Popover activation
	$('[data-toggle="popover"]').popover({html: true});
});

/**
 * The following snippet helps dismissing popovers after their activation,
 * allowing only one popover to be displayed.
 *
 * Source:
 * https://stackoverflow.com/questions/11703093/how-to-dismiss-a-twitter-bootstrap-popover-by-clicking-outside
 */
$(document).on('click', function (e) {
	$('[data-toggle="popover"],[data-original-title]').each(function () {
		//the 'is' for buttons that trigger popups
		//the 'has' for icons within a button that triggers a popup
		if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
			(($(this).popover('hide').data('bs.popover')||{}).inState||{}).click = false  // fix for BS 3.3.6
		}
	});
});
