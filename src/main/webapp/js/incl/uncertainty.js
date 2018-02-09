/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.uncertainty = ND.uncertainty || {};

$(document).ready(function () {
	var $inputs = $("input[data-classes~='technique_uncertainty']");
	var $checkboxes = $("input[name='method.technique_uncertainty_unknown']");

	if ($inputs.length > 0) {
		$inputs.change(ND.uncertainty.updateUncertaintyInput);
		$checkboxes.change(ND.uncertainty.updateUncertaintyUnknownCheckbox);

		$inputs.change();
	}
});

ND.uncertainty.findInputs = function ($input) {
	var $div = $input.closest("div.form-group");

	return [$div.find("input[data-classes~='technique_uncertainty']"),
		$div.find("input[name='method.technique_uncertainty_unknown']")];
};

ND.uncertainty.updateUncertaintyInput = function () {
	var $input = $(this);
	var $checkbox = ND.uncertainty.findInputs($input)[1];
	var unknown = parseFloat($input.val()) <= 0;

	$input.addClass(unknown ? "hidden" : "");
	$checkbox.prop("checked", unknown);

	//$input.prop("disabled", $checkbox.prop("checked"));
};

ND.uncertainty.updateUncertaintyUnknownCheckbox = function () {
	var $checkbox = $(this);
	var $input = ND.uncertainty.findInputs($checkbox)[0];
	var currentInput = parseFloat($input.val());
	var checkboxChecked = $checkbox.prop("checked");

	// If input valid, abort
	if ((checkboxChecked && currentInput <= 0) || (!checkboxChecked && currentInput > 0)) {
		return;
	}

	$input.removeClass("hidden").addClass(checkboxChecked ? "hidden" : "");
	$input.val(checkboxChecked ? "0" : "0.1");
};
