package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDate;

public record PublicProgramStatusResponse(
        String slug,
        String name,
        String accreditationStatus,
        LocalDate validFrom,
        LocalDate validUntil,
        boolean certificateAvailable
) {
}
