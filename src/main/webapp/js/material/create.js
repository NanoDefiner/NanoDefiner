/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.material = ND.material || {};
ND.material.create = ND.material.create || {};

/**
 * Called each time the material creation form was updated.
 *
 * This function will set the changed flag to <code>true</code> and save
 * the change time. The information are used in {@link #liveFeedbackLoop()}.
 */
ND.material.create.updateForm = function () {
	$(this).closest('form').data('changed', true);
	$(this).closest('form').data('changedTime', ND.util.getTime());
	// $("#feedback span").fadeTo('100', 0); // fade animation 100ms duration
};

/**
 * This function will periodically check if the form was changed and request
 * updated technique availability information from the server.
 */
ND.material.create.liveFeedbackLoop = function () {
	$form = $('#materialCreateForm');
	// Request information only when the form has changed and it has not been
	// changed within the last n milliseconds seconds

	if ($form.data('changed')
			&& (ND.util.getTime() - $form.data('changedTime'))
			> ND.material.create.liveFeedbackLoop.options.pollTime) {
		ND.material.create.requestLiveFeedback();
		$('#feedbackInProgress').removeClass('hidden');
		$form.data('changed', false);
	}
};

ND.material.create.liveFeedbackLoop.options = {
	pollTime: 50, // polling 50ms
	timeout: 5000 // consider response timed out after 5 secs
};

/**
 * Requests technique availability information from the server.
 */
ND.material.create.requestLiveFeedback = function () {
	var $form = $("#materialCreateForm");
	// Keep track of request sequence
	var i = ($form.data('_iOut') || 0) + 1;
	$form.data('_iOut', i);
	// TODO this is a workaround for slider position not being set when the
	// control is still active
	ND.material.create.updateSizeRange();
	ND.material.create.updateAnalysisTemperature();
	// Reset table filters
	$.post($("#liveFeedbackUri").text(),
			$form.serialize() + '&_i=' + i, ND.material.create.liveFeedbackHandler);
};

/**
 * Handles technique availability updates from the server.
 */
ND.material.create.liveFeedbackHandler = function (data) {
	// $("#feedback span").fadeTo(100, 0); // fade animation 100ms
	if (!ND.material.create.checkResponse(data.meta)) {
		return;
	}

	$.each(data.suitability, ND.material.create.updateTechniqueSuitability);
	ND.material.create.updateTechniqueModals(data.compatibilityMap);
	ND.material.create.updateIncompleteness(data.incompleteness);
	ND.material.create.setupMcsImages();
	// $("#feedback span").fadeTo(100, 1); // fade animation 100ms
};

/**
 * Check whether the incoming response is ahead of the current form state.
 * @param meta collection of metadata of the response
 * @returns {boolean} True if the response may be processed further, false
 * otherwise
 */
ND.material.create.checkResponse = function (meta) {
	var $form = $("#materialCreateForm");
	var iIn = ($form.data('_iIn') || 0);
	var iOut = ($form.data('_iOut') || 1);
	var iMeta = parseInt(meta.i);

	// If we got a response to our last request or too much time has passed,
	// remove animated icon
	if (iMeta === iOut ||
			(ND.util.getTime() - $form.data('changedTime'))
			> ND.material.create.liveFeedbackLoop.options.timeout) {
		$('#feedbackInProgress').addClass('hidden');
	}

	if (iMeta > iIn) {
		$form.data('_iIn', meta.i);

		return true;
	}

	return false;
};

ND.material.create.getMaterialSignifier = function() {
	return $("#materialCreateForm").find('#material\\.material_signifier :selected').val();
};

/**
 * Updates the suitability of one technique.
 *
 * @param key Technique ID
 * @param value Availability, one of "success", "warning", "danger", or
 *  "info".
 */
ND.material.create.updateTechniqueSuitability = function (key, value) {
	var id = "#" + key;
	var $span = $(id);
	$span.removeClass('alert-info alert-success alert-warning alert-danger');
	$span.addClass('alert-' + value);
	var unassessed = $span.hasClass("unassessed");

	var $glyphicon = $(id + "-glyphicon");

	$glyphicon.removeClassesWithPrefix('glyphicon-');

	if (value === 'success')
		$glyphicon.addClass(unassessed ? 'glyphicon-ok-circle' : 'glyphicon-ok-sign');
	else if (value === 'warning')
		$glyphicon.addClass('glyphicon-question-sign');
	else if (value === 'danger')
		$glyphicon.addClass('glyphicon-remove-sign');
	else
		$glyphicon.addClass('glyphicon-warning-sign');

	$span.show();
};

ND.material.create.updateTechniqueModals = function (compatibilityMap) {
	var techniques = [];

	$('div[data-classes ~= "current-material"] table[data-classes ~= "technique-modal-table"]')
			.each(function () {
				var rows = $(this).bootstrapTable('getOptions').data;
				var technique = $(this).attr("data-technique");
				techniques.push(technique);

				var attribute, attributeValue, matching, matchStatus;
				var $material, $attribute, $glyphicon;
				for (var i = 0; i < rows.length; i++) {
					attribute = rows[i]["_data"].attribute;
					$attribute = $('<div />', {html: rows[i].attribute});
					$material = $('<div />', {html: rows[i].material});

					attributeValue = compatibilityMap[attribute]["value"][0];
					matchStatus = compatibilityMap[attribute][technique][0];

					rows[i]["_attribute_class"] = "bg-"
							+ ND.material.create.determineAttributeColumnBackground(matchStatus);

					$material.find("span[data-classes ~= attribute-value]")
							.html(attributeValue);
					$material.find("span[data-classes ~= suitability]")
							.html("");
					$material.find("span[data-classes ~= matching]")
							.html("x-" + matchStatus);

					$glyphicon = $attribute.find("span[class ~= glyphicon]");
					$glyphicon.removeClassesWithPrefix("glyphicon-");
					$glyphicon.addClass("glyphicon-" + ND.material.create.determineGlyphicon(matchStatus));

					rows[i].material = $material.html();
					rows[i].attribute = $attribute.html();
					rows[i].explanation = compatibilityMap[attribute][technique][1];
				}

				$(this).bootstrapTable('load', rows);
			});

	// Update modal targets according to material signifier
	var materialSignifier = ND.material.create.getMaterialSignifier();
	var modalId, techniqueId;

	for (var i = 0; i < techniques.length; i++) {
		techniqueId = "#" + techniques[i];
		modalId = techniqueId + materialSignifier + "Modal";

		if ($(modalId).length === 0) {
			modalId = techniqueId + "defaultModal";
		}

		$("#" + techniques[i]).attr('data-target', modalId);
	}
};

/**
 * Re-implementation of the same method in MaterialTemplateService.
 *
 * TODO pass this data along with the ajax response to avoid redundant code?
 */
ND.material.create.determineAttributeColumnBackground = function (matchStatus) {
	switch (matchStatus) {
		case 'match':
			return "success";
		case 'mismatch':
			return "danger";
		case 'incompleteness_material':
		case 'incompleteness_technique':
			return "warning";
		default:
			return "faded";
	}
};

/**
 * Re-implementation of the same method in MaterialTemplateService.
 *
 * TODO pass this data along with the ajax response to avoid redundant code?
 */
ND.material.create.determineGlyphicon = function (matchStatus) {
	switch (matchStatus) {
		case 'match':
			return "ok-sign";
		case 'mismatch':
			return "remove-sign";
		case 'incompleteness_material':
		case 'incompleteness_technique':
			return "question-sign";
		default:
			return "none";
	}
};

ND.material.create.updateIncompleteness = function (incompleteness) {

	var incompletenessValue, prefix;
	var materialSignifier = ND.material.create.getMaterialSignifier();
	for (var key in incompleteness) {

		prefix = (key === "material" ? key : key + materialSignifier);

		incompletenessValue = Math.round(incompleteness[key] * 100);

		ND.material.create.updateProgressBar(prefix + 'Incompleteness',
				incompletenessValue);
	}
};

ND.material.create.updateProgressBar = function (id, incompleteness) {
	var $progressBar = $("#" + id);
	$progressBar.removeClass("progress-bar-success progress-bar-warning progress-bar-danger");
	$progressBar.text(incompleteness + "%");
	$progressBar.css("width", incompleteness + "%").attr("aria-valuenow", incompleteness);

	if (incompleteness > 50) {
		$progressBar.addClass("progress-bar-danger");
	} else if (incompleteness <= 20) {
		$progressBar.addClass("progress-bar-success");
	} else {
		$progressBar.addClass("progress-bar-warning");
	}
};

ND.material.create.saveSelectedChemicalComposition = function () {
	$("#materialCreateForm").data('chemicalComposition',
			ND.form.getSelectedOptions($("#select-chemical_composition")));
};

/**
 * Called after the chemical composition has been changed.
 */
ND.material.create.updateChemicalComposition = function () {
	var select = $("#select-chemical_composition").first();
	var options = ND.form.getSelectedOptions(select);
	var optionsOld = $("#materialCreateForm").data('chemicalComposition');

	// If nothing is chosen, choose unknown
	if (select.find(":checked").length === 0) {
		ND.form.setSelectpickerValue(select, '?');
	}

	// If more than one was selected, make sure unknown is not selected
	if (select.find(":checked").length > 1) {
		if (ND.util.getNewElement(optionsOld, options) !== '?') {
			options = ND.util.removeElement(options, "?");
		}
		else {
			options = '?';
		}

		ND.form.setSelectpickerValue(select, options);
	}

	// Check again if there are still more than one selected
	if (select.find(":checked").length > 1) {
		var selectComposites = $("#select-composites");
		options = ND.form.getSelectedOptions(selectComposites);

		// If "no composite" is selected, change to "unknown"
		if (options.indexOf("") >= 0) {
			ND.material.create.selectUnknownComposites();
		}
	}

	ND.material.create.saveSelectedChemicalComposition();
};

/**
 * Called after the composites have been changed.
 */
ND.material.create.updateComposites = function () {
	var select = $("#select-composites").first();
	var options = ND.form.getSelectedOptions(select);
	var selectChemicalComposition = $("#select-chemical_composition").first();

	if (selectChemicalComposition.find(":checked").length > 1
			&& options.indexOf("") >= 0) {
		alert("Invalid combination of parameters: 'No composite' and "
				+ "multiple entries selected for 'Chemical composition'.");
		ND.material.create.selectUnknownComposites();
	}
};

/**
 * Helper function which deselects "no composites" and selects "unknown" for
 * composites.
 */
ND.material.create.selectUnknownComposites = function () {
	var select = $("#select-composites");
	ND.form.setSelectpickerValue(select, '?');
};

/**
 * Sets up size range slider.
 */
ND.material.create.sliderSizeRange = function () {
	$("#wr_size_range_slider").slider({
		range: true,
		min: 1,
		max: 5000,
		values: [$("#wr_size_range_lower").val(),
			$("#wr_size_range_upper").val()],
		slide: function (event, ui) {
			$("#wr_size_range_lower").val(ui.values[0]);
			$("#wr_size_range_upper").val(ui.values[1]);
			$("#wr_size_range_lower").change();
		}
	}).slider("pips", { // new
		rest: "pips",
		step: 100,
		suffix: "nm"
	});

	$("#wr_size_range_slider").find(".ui-slider-label")
			.click(ND.material.create.updateSizeRangeInputs);

	ND.material.create.updateSizeRangeInputs();
};

/**
 * Toggles disabled states for sliders and associated inputs.
 */
ND.material.create.toggleSliderUnknown = function () {

	var unknown = $(this).prop('checked');

	$('input[type=number][name="' + $(this).attr('name') + '"]')
			.prop('disabled', unknown);
	$(this).closest('div.form-group').find('[data-classes~=slider]')
			.slider({disabled: unknown});
};

/**
 * Synchronizes the inputs with the size range slider.
 */
ND.material.create.updateSizeRangeInputs = function () {
	$("#wr_size_range_lower").val($("#wr_size_range_slider").slider("values", 0));
	$("#wr_size_range_upper").val($("#wr_size_range_slider").slider("values", 1));
};

/**
 * Synchronizes the size range slider with the associated inputs.
 */
ND.material.create.updateSizeRange = function () {
	ND.material.create.checkSizeRange();
	$("#wr_size_range_slider").slider("values", 0, $("#wr_size_range_lower").val());
	$("#wr_size_range_slider").slider("values", 1, $("#wr_size_range_upper").val());
};

/**
 * Checks for valid size ranges.
 */
ND.material.create.checkSizeRange = function () {
	if ($("#wr_size_range_lower").val() > 80) {
		$("#wr_size_range_lower").val(80);
	}
};

/**
 * Sets up the analysis temperature slider.
 */
ND.material.create.sliderAnalysisTemperature = function () {
	$("#analysis_temperature_slider").slider({
		range: true,
		min: 0,
		max: 500,
		values: [$("#analysis_temperature_lower").val(), $("#analysis_temperature_upper").val()],
		slide: function (event, ui) {
			$("#analysis_temperature_lower").val(ui.values[0]);
			$("#analysis_temperature_upper").val(ui.values[1]);
			$("#analysis_temperature_lower").change();
		}
	}).slider("pips", { // new
		rest: "pip",
		step: 50,
		suffix: "&deg;C"
	});

	$("#analysis_temperature_slider .ui-slider-label")
			.click(ND.material.create.updateAnalysisTemperatureInputs);

	ND.material.create.updateAnalysisTemperatureInputs();
};

/**
 * Synchronizes the inputs with the analysis temperature slider.
 */
ND.material.create.updateAnalysisTemperatureInputs = function () {
	$("#analysis_temperature_lower").val($("#analysis_temperature_slider").slider("values", 0));
	$("#analysis_temperature_upper").val($("#analysis_temperature_slider").slider("values", 1));
};

/**
 * Synchronizes the analysis temperature slider with the associated inputs.
 */
ND.material.create.updateAnalysisTemperature = function () {
	$("#analysis_temperature_slider").slider("values", 0, $("#analysis_temperature_lower").val());
	$("#analysis_temperature_slider").slider("values", 1, $("#analysis_temperature_upper").val());
};

/**
 * Saves the magnetism selection status.
 */
ND.material.create.saveSelectedMagnetism = function () {
	$("#materialCreateForm").data('magnetism',
			ND.form.getSelectedOptions($("#select-magnetism")));
};

/**
 * Called after the magnetism has been changed.
 */
ND.material.create.updateMagnetism = function () {
	var select = $("#select-magnetism").first();
	var options = ND.form.getSelectedOptions(select);
	var optionsOld = $("#materialCreateForm").data('magnetism');

	// If nothing is chosen, choose unknown
	if (select.find(":checked").length === 0) {
		ND.form.setSelectpickerValue(select, '?');
	}

	// If more than one was selected, make sure unknown or none is not selected
	if (select.find(":checked").length > 1) {
		if (ND.util.getNewElement(optionsOld, options) === '?') {
			options = '?';
		} else if (ND.util.getNewElement(optionsOld, options) === '') {
			options = '';
		} else {
			options = ND.util.removeElement(options, '?');
			options = ND.util.removeElement(options, '');
		}

		ND.form.setSelectpickerValue(select, options);
	}

	ND.material.create.saveSelectedMagnetism();
};

ND.material.create.loadTemplate = function () {
	var $selected = $("#template").find(":selected");
	var defaultName = $("#defaultMaterialName").text();

	if ($selected.val() !== "0" && defaultName !== "") {
		$.post($("#loadTemplateUri").text() + $selected.val(), ND.material.create.loadTemplateHandler);

		$name = $("#name");
		$name.val(defaultName + " " + ND.util.getLocaleString("material.create.form.name.based_on")
				+ " " + $selected.text());
		$name.change();
	}
};

// TODO remove?
ND.material.create.loadMaterial = function (id) {
	$.post($("#loadTemplateUri").text() + id, ND.material.create.loadTemplateHandler);
};

ND.material.create.loadTemplateHandler = function (data) {
	$(".selectpicker[data-feedback!='false']").selectpicker('deselectAll');
	$("input[data-classes~=slider-unknown]:checked").each(function () {
		$(this).prop("checked", false);
		$(this).each(ND.material.create.toggleSliderUnknown);
	});
	$("#materialCreateForm").deserialize(data, {complete: ND.material.create.updateAllFormElements});
};

ND.material.create.dimensionsOptions = {
	unknown: '?',
	d1: '1d',
	d2: '2d',
	d3: '3d',
	Mix: 'mix'
};

ND.material.create.shapeOptions = {
	unknown: '?',
	sphere: 'spherical',
	equiaxial: 'equiaxial',
	elongated: 'elongated',
	flat: 'flat',
	mix: 'mix',
	other: 'other'
};

// Call on change of attribute shape
ND.material.create.updateShape = function () {
	var selected = $('#select-shape option:selected').attr('value');
	var dimensions = ND.material.create.dimensionsOptions.unknown; // Default 'Unknown'

	switch (selected) {
		case ND.material.create.shapeOptions.unknown:
			dimensions = ND.material.create.dimensionsOptions.unknown;
			break;
		case ND.material.create.shapeOptions.sphere:
			dimensions = ND.material.create.dimensionsOptions.d3;
			break;
		case ND.material.create.shapeOptions.equiaxial:
			dimensions = ND.material.create.dimensionsOptions.d3;
			break;
		case ND.material.create.shapeOptions.elongated: // Elongated -> 2 dimensions are small
			dimensions = ND.material.create.dimensionsOptions.d2;
			break;
		case ND.material.create.shapeOptions.flat: // Flat -> 1 dimension is small
			dimensions = ND.material.create.dimensionsOptions.d1;
			break;
		case ND.material.create.shapeOptions.mix:
			dimensions = ND.material.create.dimensionsOptions.unknown;
			break;
		case ND.material.create.shapeOptions.other:
			dimensions = ND.material.create.dimensionsOptions.unknown;
			break;
	}
	ND.form.setSelectpickerValue($('#select-dimensions'), dimensions);
};

// Call on change of attribute dimensions
ND.material.create.updateDimensions = function () {
	var selected = $('#select-dimensions option:selected').attr('value');
	var shape = $('#select-shape option:selected').attr('value'); // Not default 'Unknown'

	switch (selected) {
		case ND.material.create.dimensionsOptions.unknown:
			// Multiple possibilities -> Unknown
			shape = ND.material.create.shapeOptions.unknown;
			break;
		case ND.material.create.dimensionsOptions.d1:
			shape = ND.material.create.shapeOptions.flat;
			break;
		case ND.material.create.dimensionsOptions.d2:
			shape = ND.material.create.shapeOptions.elongated;
			break;
		case ND.material.create.dimensionsOptions.d3:
			// Multiple possibilities -> Unknown
			shape = ND.material.create.shapeOptions.unknown;
			break;
		case ND.material.create.dimensionsOptions.mix:
			// Multiple possibilities -> Unknown
			shape = ND.material.create.shapeOptions.unknown;
			break;
	}
	ND.form.setSelectpickerValue($('#select-shape'), shape);
};

ND.material.create.saveSelectedDispersibility = function () {
	$("#materialCreateForm").data('dispersibility',
			ND.form.getSelectedOptions($("#select-dispersibility")));
};

/**
 * Called after the dispersibility has been changed.
 */
ND.material.create.updateDispersibility = function () {
	var select = $("#select-dispersibility").first();
	var options = ND.form.getSelectedOptions(select);
	var optionsOld = $("#materialCreateForm").data('dispersibility');

	// If nothing is chosen, choose unknown
	if (select.find(":checked").length === 0) {
		ND.form.setSelectpickerValue(select, '?');
	}

	var newElement = ND.util.getNewElement(optionsOld, options);

	// If the new element was either empty or unknown, deselect everything else
	if (newElement === '' || newElement === '?') {
		ND.form.setSelectpickerValue(select, newElement);
	} else if (newElement !== null) {
		// Otherwise make sure that unknown and empty are not selected
		options = ND.util.removeElement(options, '?');
		options = ND.util.removeElement(options, '');

		ND.form.setSelectpickerValue(select, options);
	}

	ND.material.create.saveSelectedDispersibility();
};

ND.material.create.preSubmitFormCheck = function () {
	$modal = $('#materialCreateFormModal');

	if (($modal.data('bs.modal') || {}).isShown) {
		return true;
	}

	$form = $(this);
	var serialized = $form.serializeArray();
	// TODO solve more flexible
	var blacklist = ['material.parent', 'material.name', 'material.comment'];
	var unknownAttributes = [];

	var i;
	for (i = 0; i < serialized.length; i++) {

		if (blacklist.indexOf(serialized[i].name) >= 0) {
			continue;
		}

		if (serialized[i].value.indexOf('?') >= 0) {
			unknownAttributes.push(i);
		}
	}

	if (unknownAttributes.length === 0) {
		return true;
	}

	// Remove current list of unknown attributes
	$unknownAttributeList = $('#unknownAttributeList');
	$unknownAttributeList.empty();

	var attributeName;
	for (i = 0; i < unknownAttributes.length; i++) {
		attributeName = serialized[unknownAttributes[i]].name;
		$unknownAttributeList.append('<li>'
				+ $('#' + attributeName.replace('.', '\\.') + '\\.label').text() + '</li>');
	}

	$modal.modal('show');

	return false;
};

ND.material.create.updateAllFormElements = function () {
	// TODO move these into object and iterate over them
	ND.material.create.updateChemicalComposition();
	ND.material.create.updateDispersibility();
	ND.material.create.updateMagnetism();

	ND.material.create.updateSizeRange();
	ND.material.create.updateAnalysisTemperature();

	$('.selectpicker').selectpicker('refresh');

	ND.material.create.requestLiveFeedback();
};

/**
 * Initializes form cache.
 *
 * The form cache contains old values for multi-selects.
 */
ND.material.create.initFormCache = function () {
	ND.material.create.saveSelectedChemicalComposition();
	ND.material.create.saveSelectedDispersibility();
	ND.material.create.saveSelectedMagnetism()
};

ND.material.create.setupMcsImages = function () {
	$("select").on('show.bs.select', function (e) {
		$(this).find("option[data-tokens]").each(function () {
			var dataTokens = $(this).attr('data-tokens');
			var $a = $("div.bootstrap-select a[data-tokens~='" + dataTokens + "']");
			$a.find("img.mcs-image").remove();
			$a.append($("#mcsImages").find("[data-tokens~='" + dataTokens + "']").clone());
		});
	});
};

$(document).ready(function () {

	// Templates
	$("#template").change(ND.material.create.loadTemplate);

	// Set up sliders
	$("#wr_size_range_lower").change(ND.material.create.updateSizeRange);
	$("#wr_size_range_upper").change(ND.material.create.updateSizeRange);
	ND.material.create.sliderSizeRange();

	$("#analysis_temperature_lower").change(ND.material.create.updateAnalysisTemperature);
	$("#analysis_temperature_upper").change(ND.material.create.updateAnalysisTemperature);
	ND.material.create.sliderAnalysisTemperature();

	$("input[data-classes~=slider-unknown]").click(ND.material.create.toggleSliderUnknown)
			.each(ND.material.create.toggleSliderUnknown);

	// Initialization for change handlers
	ND.material.create.initFormCache();
	$("#select-chemical_composition").change(ND.material.create.updateChemicalComposition);
	$("#select-composites").change(ND.material.create.updateComposites);
	$("#select-magnetism").change(ND.material.create.updateMagnetism);
	$('#select-shape').change(ND.material.create.updateShape);
	$('#select-dimensions').change(ND.material.create.updateDimensions);
	$('#select-dispersibility').change(ND.material.create.updateDispersibility);

	// Set up live feedback
	$materialCreateForm = $('#materialCreateForm');
	$materialCreateForm.find(":input").not("[data-feedback='false']")
			.change(ND.material.create.updateForm);
	var liveFeedbackInterval = setInterval(ND.material.create.liveFeedbackLoop, 50);
	$materialCreateForm.data('changed', true);
	$materialCreateForm.data('changedTime', ND.util.getTime());
	$materialCreateForm.data('liveFeedbackInterval', liveFeedbackInterval);

	// Setup pre-submit check
	$materialCreateForm.submit(ND.material.create.preSubmitFormCheck);
	$materialCreateForm.find('button[type=submit]').click(function () {
		$materialCreateForm.data('lastSubmitButton', $(this));
		return true;
	});
	$('#materialCreateFormModalSubmit').click(function () {
		$materialCreateForm.data('lastSubmitButton').click();
	});

	/**
	 * Collapse group for legend of availability markers.
	 */
	$('.collapse-group .collapse-btn').on('click', function (e) {
		e.preventDefault();
		var $this = $(this);
		var $collapse = $this.closest('.collapse-group').find('.collapse');
		$collapse.collapse('toggle');
	});

	// Set up mcs images
	ND.material.create.setupMcsImages();

	// Check if a material is to be loaded, used for creating new templates based
	// on existing ones
	var loadMaterialId = $("#loadMaterialId");

	if (loadMaterialId.length > 0) {
		ND.material.create.loadMaterial(loadMaterialId.text());
	}

	$("#name").change();
});
