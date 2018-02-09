/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.multistep = ND.multistep || {};

/*
 * Please note: A multistep form accepts the return key by default as a submit.
 *
 * As this happens rather often as an accident then as a concrete action, this
 * should be blocked. As several JavaScript implementations are not consistent
 * for every browser and browser version, the following quick and dirty hack
 * might help preventing this.
 *
 * By adding the following line of code as the first element of a form, an
 * accidental submit will be prevented:
 *
 * HTML:
 * <input class="invisible" type="submit" onclick="return false;" />
 *
 * CSS:
 * .invisible {
 * 		display: none !important;
 * 		visibility: hidden !important;
 * }
 *
 * This happens because always the first submit button is triggered by hitting
 * return. This is blocked via the onclick attribute. The class .invisible
 * should hide the button completely, so the form does look as it should.
 *
 * Source:
 * http://stackoverflow.com/a/1430170
 */

$(document).ready(function () {
	var current = 1;

	divtopic = $(".topic");
	divhstep = $(".multistep .hstep");
	divstep = $(".multistep .step");

	btnnext = $(".multistep .next");
	btnback = $(".multistep .back");
	btnsubmit = $(".multistep .submit");

	// Init buttons and UI
	divhstep.not(':eq(0)').hide();
	divstep.not(':eq(0)').hide();
	divtopic.not(':eq(0)').hide();

	// Extension for live feedback
	divfeedback = $("div.feedback");
	divfeedback.hide();

	ND.multistep.hideButtons(current);
	ND.multistep.setProgress(current);

	// Next button click action
	btnnext.click(ND.multistep.next);

	// Back button click action
	btnback.click(ND.multistep.back);

	/*
	 * TODO Validate
	 *
	 * We need an appropriate way to validate inputs with the same name here.
	 * Right now, when there are inputs with the same name, only the first
	 * one gets validated properly.
	 */
	$('.form.multistep').validate({ // initialize plugin
		ignore: ":not(:visible)",
		rules: {
			name: "required"
		},
	});

});

ND.multistep.next = function() {
	var $form = $(".form.multistep");
	var current = $form.data('current');
	if (current < $(".multistep .step").length) {
		// Check validation
		if ($form.valid()) {

			current = ND.multistep.updateFormState(current)
		}
	}
	ND.multistep.hideButtons(current);
};

ND.multistep.back = function() {
	var $form = $(".form.multistep");
	var current = $form.data('current');
	if (current > 1) {
		current = current - 2;
		if (current < $(".multistep .step").length) {

			current = ND.multistep.updateFormState(current)
		}
	}
	ND.multistep.hideButtons(current);
};

// Change progress bar action
ND.multistep.setProgress = function(currstep) {
	$(".form.multistep").data('current', currstep);
	var percent = parseFloat(100 / divstep.length) * currstep;
	percent = percent.toFixed();

	// We don't need percentage values on the progress bar
	$(".progress-bar-form").css("width", percent + "%"); // .html(percent+"%");
};

// Hide buttons according to the current step
ND.multistep.hideButtons = function(current) {
	var limit = parseInt(divstep.length);

	$(".multistep .action").hide();

	if (current < limit) btnnext.show();
	if (current > 1) btnback.show();
	if (current === limit) {
		btnnext.hide();
		btnsubmit.show();
	}
};

ND.multistep.updateFormState = function(current) {
	var $divtopic = $(".topic");
	var $divhstep = $(".multistep .hstep");
	var $divstep = $(".multistep .step");
	var $divfeedback = $("div.feedback");

	$divtopic.show();
	$divtopic.not(':eq(' + (current) + ')').hide();

	$divhstep.show();
	$divhstep.not(':eq(' + (current) + ')').hide();

	$divstep.show();
	$divstep.not(':eq(' + (current) + ')').hide();

	// Extension for live feedback
	$divfeedback.hide();
	if (current !== 0)
		$divfeedback.show();

	current++;
	ND.multistep.setProgress(current);

	return current;
};
