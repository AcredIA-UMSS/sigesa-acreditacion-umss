package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record ProgramSummaryResponse(
        UUID id,
        String code,
        String name
) {
}
