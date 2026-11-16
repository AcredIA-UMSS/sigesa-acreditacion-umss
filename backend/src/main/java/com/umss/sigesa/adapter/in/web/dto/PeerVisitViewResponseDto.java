package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record PeerVisitViewResponseDto(
        UUID processId,
        String careerName,
        PeerVisitResponseDto visit
) {
}
