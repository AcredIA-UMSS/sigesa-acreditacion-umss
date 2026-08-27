package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Subphase;

import java.util.UUID;

public interface UpdateProcessSubphaseUseCase {

    Subphase execute(UUID processId, UUID phaseId, UUID subphaseId,
                     String name, Integer order, String referenceUrl, String description,
                     String requirements);
}
