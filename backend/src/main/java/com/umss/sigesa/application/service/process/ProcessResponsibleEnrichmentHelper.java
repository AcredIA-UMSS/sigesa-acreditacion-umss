package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ProcessResponsibleEnrichmentHelper {

    private ProcessResponsibleEnrichmentHelper() {
    }

    public static Optional<ProcessResponsibleInfo> resolveForProcess(UUID processId,
                                                                     ProcessResponsiblePort processResponsiblePort,
                                                                     UserRepositoryPort userRepositoryPort) {
        Optional<ProcessResponsibleAssignment> assignment =
                processResponsiblePort.findActiveByProcessId(processId);
        if (assignment.isEmpty()) {
            return Optional.empty();
        }
        return userRepositoryPort.findById(assignment.get().getUserId())
                .map(user -> toInfo(user, assignment.get().getAssignedAt()));
    }

    public static Map<UUID, ProcessResponsibleInfo> resolveForProcesses(
            List<UUID> processIds,
            ProcessResponsiblePort processResponsiblePort,
            UserRepositoryPort userRepositoryPort) {
        if (processIds == null || processIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, ProcessResponsibleInfo> result = new HashMap<>();
        for (ProcessResponsibleAssignment assignment : processResponsiblePort.findAllActive()) {
            if (!processIds.contains(assignment.getProcessId())) {
                continue;
            }
            userRepositoryPort.findById(assignment.getUserId())
                    .ifPresent(user -> result.put(
                            assignment.getProcessId(),
                            toInfo(user, assignment.getAssignedAt())));
        }
        return result;
    }

    private static ProcessResponsibleInfo toInfo(AppUser user, java.time.LocalDateTime assignedAt) {
        return new ProcessResponsibleInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail().value(),
                assignedAt
        );
    }
}
