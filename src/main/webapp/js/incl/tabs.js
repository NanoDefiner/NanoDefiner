/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.tabs = ND.tabs || {};

ND.tabs.enableExternalTabLinks = function() {

	var $tabActivators = $('[data-toggle="tab"]');

	if ($tabActivators.length === 0) return;

	$tabActivators.on('shown.bs.tab', function (e) {
		var target = this.href.split('#');

		var element = $(".modal.in").length === 1 ? ".modal.in" : window;

		var tempScrollTop = $(element).scrollTop();

		if (window.location.hash !== target[1]) {
			window.location.hash = target[1];
			$('.nav > li > a').filter('[href="#' + target[1] + '"]').tab('show');
		}

		$(element).scrollTop(tempScrollTop);
	});

	$tabActivators.click(function (e) {
		e.preventDefault();
		window.history.replaceState({}, '', this.href);
		window.location = this.href;
		ND.tabs.hashChanged(window.location.hash);
	});

	if (window.location.hash === "") {
		$tabLink = $($('a[data-toggle="tab"]').get(0));

		// Only make tab active if it's not in a modal
		if ($tabLink.parents(".modal").length === 0) {
			$tabLink.click();
		}

		//window.history.replaceState({}, '', );
		//hashChanged(window.location.hash);
	}

	var $anchor = $("#variables-anchor");
	if ($anchor.length > 0 && $anchor.text() !== "") {
		$('a[href="#' + $anchor.text() + '"]').click();
	}

	if ("onhashchange" in window) { // event supported?
		window.onhashchange = function () {
			ND.tabs.hashChanged(window.location.hash);
		}
	}
	else { // event not supported:
		var storedHash = window.location.hash;
		window.setInterval(function () {
			if (window.location.hash !== storedHash) {
				storedHash = window.location.hash;
				ND.tabs.hashChanged(storedHash);
			}
		}, 100);
	}

	$tabLink = $('a[href="' + window.location.hash + '"]');

	if ($tabLink.length > 0) {
		$tabLink.get(0).click();
	}

	ND.tabs.hashChanged(window.location.hash);
	$(window).scrollTop(0);
};

ND.tabs.updateURLParameter = function(url, param, paramVal) {

	var anchor = null;
	var newAdditionalURL = "";
	var tempArray = url.split("?");
	var baseURL = tempArray[0];
	var additionalURL = tempArray[1];
	var temp = "";
	var tmpAnchor;
	var params;

	paramVal = encodeURIComponent(paramVal);

	if (additionalURL) {
		tmpAnchor = additionalURL.split("#");
		params = tmpAnchor[0];
		anchor = tmpAnchor[1];
		if (anchor)
			additionalURL = params;

		tempArray = additionalURL.split("&");

		for (var i = 0; i < tempArray.length; i++) {
			if (tempArray[i] !== "" && tempArray[i].split('=')[0] !== param) {
				newAdditionalURL += temp + tempArray[i];
				temp = "&";
			}
		}
	}
	else {
		tmpAnchor = baseURL.split("#");
		params = tmpAnchor[0];
		anchor = tmpAnchor[1];

		if (params)
			baseURL = params;
	}

	if (anchor) {
		paramVal += "#" + anchor;
	}

	var rows_txt = temp + "" + param + "=" + paramVal;
	return baseURL + "?" + newAdditionalURL + rows_txt;
};

ND.tabs.hashChanged = function(hash) {
	if (hash === "") return;
	var newHref = ND.tabs.updateURLParameter(window.location.toString(), "anchor", hash.substr(1));
	window.history.replaceState({}, '', newHref);
	window.location = newHref;
};

$(document).ready(ND.tabs.enableExternalTabLinks);
