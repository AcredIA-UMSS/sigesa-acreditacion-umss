package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record ExportJobRequest(
        @NotNull String format,
        Integer phaseId
) {}
