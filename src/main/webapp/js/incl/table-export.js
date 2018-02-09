/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.tableExport = ND.tableExport || {};

ND.tableExport.blacklist = ["actions", "sort", "state"];

/**
 * Automatically excludes columns from table exports based on a blacklist and custom columns
 * defined via the data-export-exclude attribute (comma-separated list).
 */
ND.tableExport.setup = function() {
	// Collect additional custom columns to be excluded
	var columns = ND.tableExport.blacklist;
	var customColumns = $(this).attr("data-export-exclude");

	if (typeof customColumns !== "undefined") {
		columns = columns.concat(customColumns.split(","));
	}

	// Avoid empty columns in the beginning if detail view / check boxes are enabled
	var $thDetail = $(this).find("th.detail");
	var $thCheckbox = $(this).find("th.bs-checkbox");

	if ($thDetail.length > 0) {
		columns.push($thDetail.index());
	}

	if ($thCheckbox.length > 0) {
		columns.push($thCheckbox.index());
	}

	$(this).bootstrapTable('refreshOptions', {
		exportOptions: {
			ignoreColumn: columns
		}
	});
};

$(document).ready(function () {
	$("table[data-show-export=true]").each(ND.tableExport.setup);
});
