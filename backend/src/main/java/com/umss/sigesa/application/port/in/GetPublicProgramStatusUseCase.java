package com.umss.sigesa.application.port.in;

import java.time.LocalDate;

public interface GetPublicProgramStatusUseCase {

    record PublicProgramStatus(
            String slug,
            String name,
            String accreditationStatus,
            LocalDate validFrom,
            LocalDate validUntil,
            boolean certificateAvailable
    ) {}

    PublicProgramStatus getPublished(String slug);
}
