/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.feedback = ND.feedback || {};

/**
 * Handles AJAX form submission of user feedback.
 */

ND.feedback.setupFeedbackForm = function() {
	$("#feedbackModal").find("button.submit").click(ND.feedback.submitFeedback);
};

ND.feedback.submitFeedback = function() {

	$modal = $("#feedbackModal");
	$form = $modal.find('form');

	$.post($form.attr('action'), $form.serialize());

	$modal.modal('hide');

	alert(ND.util.getLocaleString("global.feedback.alert.thanks"));
	$("#feedback-text").text("");

	return false;
};

$(document).ready(ND.feedback.setupFeedbackForm);
