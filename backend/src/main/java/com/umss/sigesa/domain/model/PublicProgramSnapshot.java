package com.umss.sigesa.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record PublicProgramSnapshot(
        UUID id,
        String slug,
        String programName,
        String accreditationStatus,
        LocalDate validFrom,
        LocalDate validUntil,
        boolean published,
        boolean certificatePublished,
        String agency,
        String resolutionNumber
) {
}
