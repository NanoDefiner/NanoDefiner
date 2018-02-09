/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

ND.dossier = ND.dossier || {};
ND.dossier.create = ND.dossier.create || {};

$(document).ready(function() {

    $("#name").change(ND.dossier.create.updateSampleName);

    $("#multiconstituent").find("input").change(ND.dossier.create.updateSampleName);
});

ND.dossier.create.getSampleNameSuffix = function() {
    var checked = $("#multiconstituent").find("input:checked");
    if (checked.val() === "false") {
			suffix = " " + ND.util.getLocaleString("dossier.create.form.sample_suffix.mono");
    } else {
			suffix = " " + ND.util.getLocaleString("dossier.create.form.sample_suffix.multi");
    }

	return suffix;
};

ND.dossier.create.updateSampleName = function() {
    $("#sampleName").val($("#name").val() + ND.dossier.create.getSampleNameSuffix());
};
