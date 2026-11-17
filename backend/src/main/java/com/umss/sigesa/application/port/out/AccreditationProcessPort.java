package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.Optional;
import java.util.UUID;

public interface AccreditationProcessPort {
    boolean existsActiveProcessByCareerAndTemplateType(UUID careerId, String templateType);

    Optional<AccreditationProcess> findById(UUID processId);

    AccreditationProcess save(AccreditationProcess process);
}