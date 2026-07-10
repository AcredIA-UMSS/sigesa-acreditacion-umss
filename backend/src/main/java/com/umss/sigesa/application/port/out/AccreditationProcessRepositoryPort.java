package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccreditationProcessRepositoryPort {
    boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period);

    AccreditationProcess save(AccreditationProcess process);

    List<AccreditationProcess> findAll(ProcessStatus status, UUID careerId, String period);

    Optional<AccreditationProcess> findById(UUID processId);
}
