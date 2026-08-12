package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListEligibleResponsiblesUseCase {

    record EligibleResponsible(UUID userId, String fullName, String email) {
    }

    List<EligibleResponsible> listEligible(UUID processId);
}
