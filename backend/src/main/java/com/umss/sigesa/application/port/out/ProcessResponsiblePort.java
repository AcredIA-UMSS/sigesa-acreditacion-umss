package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProcessResponsiblePort {

    ProcessResponsibleAssignment save(ProcessResponsibleAssignment assignment);

    Optional<ProcessResponsibleAssignment> findActiveByProcessId(UUID processId);

    Optional<ProcessResponsibleAssignment> findActiveByUserId(UUID userId);

    void revokeActiveByProcessId(UUID processId);

    Set<UUID> findUserIdsWithActiveAssignment();

    List<ProcessResponsibleAssignment> findAllActive();
}
