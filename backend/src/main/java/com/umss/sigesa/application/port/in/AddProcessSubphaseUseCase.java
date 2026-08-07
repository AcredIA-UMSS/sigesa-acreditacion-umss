package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Subphase;

import java.util.UUID;

public interface AddProcessSubphaseUseCase {

    Subphase execute(UUID processId, UUID phaseId, String name, Integer order,
                     String referenceUrl, String description);
}
