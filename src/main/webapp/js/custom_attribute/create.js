/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

toggleValueType = function () {
	var $fileFormGroup = $("form div[data-classes~=file]");
	var $textFormGroup = $("form div[data-classes~=text]");
	var fileState = $fileFormGroup.hasClass("hidden");

	setFormGroupState($fileFormGroup, fileState);
	setFormGroupState($textFormGroup, !fileState);

	return false;
};

setFormGroupState = function ($formGroup, state) {
	if (state) {
		$formGroup.removeClass("hidden");

	} else {
		$formGroup.addClass("hidden");
	}

	$formGroup.find("input[data-classes~=value-input]").prop('disabled', !state);
};

$(document).ready(function () {
	$("form a[data-classes~=toggle-value-type]").click(toggleValueType);
	// Toggle twice for correct initialization
	toggleValueType();
	toggleValueType();
});
