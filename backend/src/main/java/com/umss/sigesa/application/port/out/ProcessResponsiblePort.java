package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;

import java.util.ArrayList;
import java.util.Collection;
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

    default List<ProcessResponsibleAssignment> findActiveByProcessIds(Collection<UUID> processIds) {
        if (processIds == null || processIds.isEmpty()) {
            return List.of();
        }
        List<ProcessResponsibleAssignment> result = new ArrayList<>();
        for (UUID id : processIds) {
            findActiveByProcessId(id).ifPresent(result::add);
        }
        return result;
    }
}
