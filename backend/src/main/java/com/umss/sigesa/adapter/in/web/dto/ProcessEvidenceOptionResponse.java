package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record ProcessEvidenceOptionResponse(
        UUID evidenceId,
        UUID indicatorId,
        String code,
        String title
) {
}
