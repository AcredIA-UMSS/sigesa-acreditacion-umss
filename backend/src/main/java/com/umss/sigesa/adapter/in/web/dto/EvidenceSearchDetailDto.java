package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenceSearchDetailDto(
        UUID evidenceId,
        String title,
        String description,
        String dimensionName,
        String criterionCode,
        String carreraName,
        LocalDateTime uploadedAt
) {}
