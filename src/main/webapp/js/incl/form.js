/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.form = ND.form || {};

ND.form.registerEntityNameInput = function ($input, entityName) {
	$input.change(function () {
		if ($input.val().length > 0) {
			$('[data-classes="' + entityName + '"]').text($input.val());
		}
	});
};

/**
 * Return an {@link Array} of selected options for the given select element.
 *
 * @param select select element
 */
ND.form.getSelectedOptions = function (select) {
	var selectedOptions = [];
	select.find("option:selected").each(function () {
		selectedOptions.push($(this).attr('value'));
	});

	return selectedOptions;
};

ND.form.setSelectpickerValue = function (select, value) {
	select.selectpicker('refresh');
	select.selectpicker('val', value);
	select.selectpicker('refresh');
};

$(document).ready(function () {
	$("form[data-entity]").each(function () {
		var entity = $(this).attr('data-entity');
		var $input = $(this).find("#name");
		ND.form.registerEntityNameInput($input, entity);
		$input.change();
	});
});
