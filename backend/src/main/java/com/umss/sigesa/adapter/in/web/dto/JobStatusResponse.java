package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        String status,
        int progressPercentage,
        String downloadUrl,
        String errorMessage
) {}
