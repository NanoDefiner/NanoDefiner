/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.report = ND.report || {};
ND.report.create = ND.report.create || {};

/**
 * Error reporting
 */
ND.report.create.reportProblem = function() {
	$("#feedback-stacktrace").text($("#errorDetails").text());
	$("#feedback-text").attr("placeholder",
			ND.util.getLocaleString("global.feedback.error.text_placeholder"));
	$("button.feedback").click();
};

ND.report.create.setupErrorReporting = function() {
	$("#reportProblem").click(ND.report.create.reportProblem)
};

$(document).ready(ND.report.create.setupErrorReporting);
