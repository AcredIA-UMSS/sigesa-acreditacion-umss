package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record ExportJobResponse(
        UUID jobId,
        String status,
        String message,
        String statusUrl
) {}
