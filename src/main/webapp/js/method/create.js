/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.method = ND.method || {};
ND.method.create = ND.method.create || {};

$(document).ready(function () {
	$("button[name=submit]").click(ND.method.create.preSubmitFormUpdate);

	if ($("#variables-action").text() !== 'update') {
		$("button.next").click(ND.method.create.techniqueSelectionCheck);
		// Only auto-update name for method creation, not when editing one
		$('#methodCreateTable').on('all.bs.table', ND.method.create.updateTechnique);
		$('.modal button[data-classes~=confirm]').click(ND.multistep.next);
	}

});

/**
 * Make sure a technique is selected before progressing through the form.
 */
ND.method.create.techniqueSelectionCheck = function() {
	// First go back in case something isn't right
	ND.multistep.back();

	// Find selected technique
	var $checkedTechnique = $("#methodCreateTable").find("input[type=radio]:checked");

	// Make sure a technique was selected
	if ($checkedTechnique.length === 0) {
		alert("Please select a technique!");
		return;
	}

	var modalShown = false;
	if ($(".form.multistep").data('current') === 1) {
		// Show modal if we have warnings for this technique
		var $element = $checkedTechnique.closest("tr");
		var row = $checkedTechnique.closest("table")
				.bootstrapTable('getData')[$element.attr("data-index")];
		modalShown = ND.method.create.showWarningModal(null, row, $element);
	}

	// If no modal was shown, user is allowed to go further
	if (!modalShown) {
		ND.multistep.next();
	}
};

/**
 * Update the method name based on the selected technique.
 */
ND.method.create.updateTechnique = function() {
	var $name = $("#name");
	var value = $("input:checked").closest("tr")
			.find("span[data-classes=technique-name]").text();

	if (value === "") {
		value = ND.util.getLocaleString("global.entity.new");
	}

	$name.val(value);
	$name.change();
};

ND.method.create.showWarningModal = function(e, row, $element) {
	if ($(row.warnings).hasClass("glyphicon-exclamation-sign")
			|| $(row.suitable).hasClass("glyphicon-remove")) {
		$($element.find("span.glyphicon-exclamation-sign, span.glyphicon-remove")
				.attr("data-target")).modal();
		return true;
	}

	return false;
};

/**
 * Set the value of the selected radio button just before submitting the form.
 */
ND.method.create.preSubmitFormUpdate = function() {
	$("input[name='technique.id']:checked").each(function () {
		$(this).attr("value", $(this).closest("tr")
				.find("span[data-classes=technique-id]").text());
	});
};
