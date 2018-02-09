/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.report = ND.report || {};
ND.report.create = ND.report.create || {};

/**
 * TODO documentation
 */

$(document).ready(function () {
	$("span[data-classes=methodId]").each(function () {
		$("td[data-methodId=" + $(this).text() + "]").closest("tr")
				.find("input[name=methodIds]").click();
	});

	$("button.next").click(ND.report.create.methodSelectionCheck);
});

/**
 * Make sure a technique is selected before progressing through the form.
 */
ND.report.create.methodSelectionCheck = function() {
	if ($("#methodSelectTable").find("input[type=checkbox]:checked").length === 0) {
		ND.multistep.back();
		alert(ND.util.getLocaleString("report.create.method.select_alert"));
	}
};
