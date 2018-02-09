/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.tables = ND.tables || {};

ND.tables.updateTableSelections = function($table) {
	var rowsSelected = $table.bootstrapTable('getSelections').length > 0;

	var $button = $("button[data-table='#" + $table.attr("id") + "']");
	$button.prop("disabled", !rowsSelected);
	$button.removeClass("btn-default btn-primary");
	$button.addClass(rowsSelected ? "btn-primary" : "btn-default");
};

ND.tables.updateModalLink = function(tableId, modalId) {

	var name = ND.tables.getTableEntityName($(tableId));

	var rowsSelected = $(tableId).bootstrapTable('getSelections');

	if (!rowsSelected.length) {
		// TODO locale string
		//alert("No rows selected.");
		return false;
	}

	var queryUri = "";

	for (i = 0; i < rowsSelected.length; i++) {
		// Remove HTML from ID cell
		queryUri += name + "Ids=" + $('<div />').html(rowsSelected[i].id).text().trim() + "&";
	}

	var $modalLink = $(modalId).find("a.btn-primary");
	var oldUri = $modalLink.attr("href").split("&" + name + "Ids");
	$modalLink.attr("href", oldUri[0] + queryUri);
};

ND.tables.getTableEntityName = function($table) {
	return $table.attr("data-entity");
};

/**
 * Makes cells which contain only one link clickable.
 */
ND.tables.addRowlinkSkipCellLinks = function () {
	$("td.rowlink-skip").each(function() {
		var $links = $(this).find("a");

		if ($links.length === 1 && !$(this).hasClass("nolink")) {
			$(this).addClass("clickable");

			// TODO probably doesn't work in IE
			$(this).click(function(e) {
				if ($(e.target).is("td")) {
					$links.each(function () {
						var evt = new MouseEvent("click", {
							bubbles: false
						});
						this.dispatchEvent(evt);
					});
				}
			});
		}
	});
};

ND.tables.setupTables = function() {
	ND.tables.fixDetailView();
	ND.tables.addRowlinkSkipCellLinks();
	ND.tables.setupTableContentFilter();
	ND.tables.enableTableModals();
	ND.tables.setupTableCheckboxes();
	ND.tables.setupModalTableButtons();
};

// Add "rowlink-skip" class to detail view cells
ND.tables.fixDetailView = function() {
	$('table.table[data-detail-view!="false"]').each(function () {
		var $table = $(this);
		var rows = $table.bootstrapTable('getOptions').data;

		$table.bootstrapTable('refreshOptions', {
			detailView: true,
			detailFormatter : ND.tables.detailFormatter
		});

		$table.find("tbody").attr("data-target", "a.rowlink");

		for (var i = 0; i < rows.length; i++) {
			var $rowlink = "", rowlinkColumn = "", $content = "", $link = "";

			// Search for rowlink
			for (var property in rows[i]) {
				if (rows[i].hasOwnProperty(property) && !property.startsWith("_")) {
					$content = $('<div />').html(rows[i][property]);
					$link = $content.find("a.rowlink");

					// Fall back to normal link if no rowlink found
					if ($link.length === 0) {
						$link = $content.find("a");
					}

					if ($link.length > 0 && ($link.hasClass("rowlink") || $rowlink === "")) {
						$rowlink = $('<div />').html($link.get(0));
						$rowlink.find("a").addClass("hidden rowlink");
					}
				}
			}

			// Either all rows have a link or none of them has
			if ($rowlink === "") {
				break;
			}

			// Apply rowlink to all columns
			for (property in rows[i]) {
				if (rows[i].hasOwnProperty(property) && !property.startsWith("_") && property !== "state"
						&& $('<div />').html(rows[i][property]).find("a").length === 0) {
					rows[i][property] += $rowlink.html();
				}
			}
		}

		$table.on("post-body.bs.table", function (data) {
			ND.tables.addRowlinkSkipToDetailView($table);
		});
		// Enable cell links when switching columns
		$table.on("column-switch.bs.table", function (field, checked) {
			ND.tables.addRowlinkSkipCellLinks();
		});

		// Reload data
		$table.bootstrapTable('load', rows);
	});
};

ND.tables.addRowlinkSkipToDetailView = function($table) {
	$table.find("a.detail-icon").parent().addClass("rowlink-skip");
};

ND.tables.detailFormatter = function(index, row, $element) {
	var $table = $element.closest("table");
	var columns = $table.bootstrapTable('getOptions')['columns'][0];

	var html = [];

	$.each(columns, function (index, column) {
		if (!column.visible) {
			html.push('<p><b>' + column.title.trim() + ':</b>&nbsp; ' + (row[column.field] !== "" ? row[column.field] : "–") + '</p>');
		}
	});

	var htmlJoined = html.join('');

	if (htmlJoined === '') {
		return ND.util.getLocaleString("global.table.detail.none");
	}

	return htmlJoined;
};

ND.tables.findColumnTitle = function($table, field) {

	var columns = $table.bootstrapTable('getOptions')['columns'][0];

	for (i = 0; i < columns.length; i++) {
		if (columns[i].field === field) {
			return columns[i].title;
		}
	}

	return "";
};

ND.tables.setupTableContentFilter = function() {
	$("td").each(ND.tables.filterTdContents);

	$("table").on("post-body.bs.table",
			function (data) {
				$(this).find("td").each(ND.tables.filterTdContents);
			});
};

ND.tables.filterTdContents = function() {
	if (!$(this).hasClass("bs-checkbox") && $(this).html().trim().length === 0) {
		$(this).text("–");
	}
};

ND.tables.addSearchResetIcons = function() {
	var $resetIcon = $('<a id="searchReset"><span class="glyphicon glyphicon-remove"></span></a>');

	$("table[data-entity]").each(function () {
		$(this).closest("div.bootstrap-table").find("div.search").append($resetIcon);
	});
};

ND.tables.setupTableCheckboxes = function() {
	$("table[data-entity]").each(function () {
		var $table = $(this);

		$(this).on("check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table",
				function (e, row) {
					ND.tables.updateTableSelections($table);
				});

		ND.tables.updateTableSelections($table);
	});

	$("td.bs-checkbox").addClass("rowlink-skip");
	$("button[data-target=table]").click(ND.tables.preSubmitUpdateCheckboxes);
};

ND.tables.enableTableModals = function() {

	// Copy buttons into the custom toolbar
	$("table[data-entity]").each(function () {
		var id = "#" + $(this).attr("id");
		var $toolbar = $(id).closest("div.bootstrap-table").find("div.bs-bars");

		$toolbar.append($("button[data-table='" + id + "']"));
		$toolbar.find("button").removeClass("hidden");
	});

	// Update modal link prior to showing the modals
	$("button[data-toggle=modal]").click(function () {
		ND.tables.updateModalLink($(this).attr("data-table"), $(this).attr("data-target"),
				$($(this).attr("data-table")).attr("data-entity"));
	});
};

ND.tables.getEntityId = function($checkbox) {
	var $td = $checkbox.closest("td");
	return $td.find("span").text();
};

ND.tables.preSubmitUpdateCheckboxes = function() {
	$("table tbody input[type=checkbox]").each(function () {
		var $table = $(this).closest("table");
		var $tr = $(this).closest("tr");
		var row = $table.bootstrapTable('getData')[$tr.attr("data-index")];
		$(this).attr("value", row.id);
	});
};

/**
 * TODO does this have to be in this file?
 */
ND.tables.setupModalTableButtons = function() {
	$('button[data-classes=x-all]').click(function () {
		ND.tables.setTableSearchText($(this), "");
	});

	$('div[data-classes=search-buttons] button').click(function () {
		var dataClasses = $(this).attr('data-classes');

		if (dataClasses !== 'x-all') {
			ND.tables.setTableSearchText($(this), dataClasses);
		}
	});

	$('div.search input').change(function () {
		var $toolbar = $(this).closest('div.fixed-table-toolbar');
		var value = $(this).val();

		$toolbar.find('button').removeClass('selected');

		if (value === '') {
			$toolbar.find('button[data-classes~=x-all]').addClass('selected');
		}
		else {
			$toolbar.find('button[data-classes~=' + value + ']').addClass('selected');
		}
	}).change();
};

ND.tables.setTableSearchText = function($button, text) {
	var $input = $($button.closest("div.fixed-table-toolbar")
			.find("div.search input"));

	$input.val(text);
	$input.keyup();
	$input.change();
};

$(document).ready(ND.tables.setupTables);
