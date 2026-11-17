package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.in.AssignProcessResponsibleUseCase;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.CareerScopeMismatchException;
import com.umss.sigesa.domain.exception.CcAlreadyAssignedToProcessException;
import com.umss.sigesa.domain.exception.InvalidResponsibleUserException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class AssignProcessResponsibleService implements AssignProcessResponsibleUseCase {

    private final ProcessQueryPort processQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepositoryPort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final ProcessStructureGuard processStructureGuard;
    private final EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    public AssignProcessResponsibleService(ProcessQueryPort processQueryPort,
                                           UserRepositoryPort userRepositoryPort,
                                           UserProgramAssignmentRepositoryPort assignmentRepositoryPort,
                                           ProcessResponsiblePort processResponsiblePort,
                                           ProcessStructureGuard processStructureGuard,
                                           EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        this.processQueryPort = processQueryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.assignmentRepositoryPort = assignmentRepositoryPort;
        this.processResponsiblePort = processResponsiblePort;
        this.processStructureGuard = processStructureGuard;
        this.enqueueDomainNotificationUseCase = enqueueDomainNotificationUseCase;
    }

    @Override
    public ProcessResponsibleInfo assign(UUID processId, UUID userId, UUID assignedByUserId) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        processStructureGuard.ensureProcessActive(process);

        AppUser user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new InvalidResponsibleUserException("Usuario responsable no encontrado."));
        ensureEligibleCc(user);
        ensureCareerScope(userId, process.getCareerId());
        ensureNotAssignedElsewhere(processId, userId);

        UUID previousUserId = processResponsiblePort.findActiveByProcessId(processId)
                .map(ProcessResponsibleAssignment::getUserId)
                .orElse(null);

        processResponsiblePort.revokeActiveByProcessId(processId);

        ProcessResponsibleAssignment saved = processResponsiblePort.save(ProcessResponsibleAssignment.builder()
                .processId(processId)
                .userId(userId)
                .assignedBy(assignedByUserId)
                .assignedAt(LocalDateTime.now())
                .build());

        String occurrenceId = saved.getId() != null
                ? saved.getId().toString()
                : UUID.randomUUID().toString();

        if (previousUserId != null && !previousUserId.equals(userId)) {
            enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                    NotificationEventType.ProcessUnassigned,
                    processId,
                    process.getCareerId(),
                    processId,
                    Map.of(
                            "processId", processId.toString(),
                            "programId", process.getCareerId().toString(),
                            "removedUserId", previousUserId.toString(),
                            "occurrenceId", occurrenceId
                    )));
        }

        enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessAssigned,
                processId,
                process.getCareerId(),
                processId,
                Map.of(
                        "processId", processId.toString(),
                        "programId", process.getCareerId().toString(),
                        "assigneeUserId", userId.toString(),
                        "occurrenceId", occurrenceId
                )));

        return toInfo(user, saved.getAssignedAt());
    }

    private void ensureEligibleCc(AppUser user) {
        if (user.getRole() != Role.CC || user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidResponsibleUserException(
                    "Solo un Coordinador de Carrera con cuenta ACTIVE puede ser responsable.");
        }
    }

    private void ensureCareerScope(UUID userId, UUID careerId) {
        boolean assignedToCareer = assignmentRepositoryPort.findActiveByUserId(userId).stream()
                .map(UserProgramAssignment::getProgramId)
                .anyMatch(careerId::equals);
        if (!assignedToCareer) {
            throw new CareerScopeMismatchException(
                    "El coordinador no está asignado a la carrera del proceso.");
        }
    }

    private void ensureNotAssignedElsewhere(UUID processId, UUID userId) {
        processResponsiblePort.findActiveByUserId(userId).ifPresent(existing -> {
            if (!processId.equals(existing.getProcessId())) {
                throw new CcAlreadyAssignedToProcessException(
                        "El coordinador ya es responsable de otro proceso activo.");
            }
        });
    }

    private ProcessResponsibleInfo toInfo(AppUser user, LocalDateTime assignedAt) {
        return new ProcessResponsibleInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail().value(),
                assignedAt
        );
    }
}
