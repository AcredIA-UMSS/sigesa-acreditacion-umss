package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record ReportJobStatusResponse(
        UUID jobId,
        String status,
        String downloadUrl,
        String errorCode
) {
}
