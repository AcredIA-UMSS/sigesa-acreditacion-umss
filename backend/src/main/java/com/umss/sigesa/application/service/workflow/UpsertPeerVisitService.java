package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.UpsertPeerVisitUseCase;
import com.umss.sigesa.application.port.out.PeerVisitPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.InvalidDateRangeException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.VisitImmutableException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PeerVisit;
import com.umss.sigesa.domain.model.PeerVisitItem;
import com.umss.sigesa.domain.model.PeerVisitStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UpsertPeerVisitService implements UpsertPeerVisitUseCase {

    private static final java.util.Set<String> WRITERS = java.util.Set.of("TD", "JD");

    private final ProcessQueryPort processQueryPort;
    private final PeerVisitPort peerVisitPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UpsertPeerVisitService(
            ProcessQueryPort processQueryPort,
            PeerVisitPort peerVisitPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.peerVisitPort = peerVisitPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public PeerVisit upsert(
            UUID processId,
            Command command,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        assertWriter(roles);
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);

        if (command.startsOn() != null && command.endsOn() != null && command.startsOn().isAfter(command.endsOn())) {
            throw new InvalidDateRangeException();
        }

        PeerVisit existing = peerVisitPort.findByProcessId(processId).orElse(null);
        if (existing != null && existing.getStatus() == PeerVisitStatus.COMPLETED) {
            throw new VisitImmutableException();
        }

        LocalDateTime now = LocalDateTime.now();
        UUID visitId = existing != null ? existing.getId() : UUID.randomUUID();
        List<PeerVisitItem> items = command.items() == null ? List.of() : command.items();
        int order = 0;
        for (PeerVisitItem item : items) {
            if (item.getId() == null) {
                item.setId(UUID.randomUUID());
            }
            item.setOrder(order++);
        }

        PeerVisit visit = PeerVisit.builder()
                .id(visitId)
                .processId(processId)
                .startsOn(command.startsOn())
                .endsOn(command.endsOn())
                .venue(command.venue())
                .room(command.room())
                .status(command.status() != null ? command.status() : PeerVisitStatus.SCHEDULED)
                .notes(command.notes())
                .createdBy(existing != null ? existing.getCreatedBy() : requesterId)
                .createdAt(existing != null ? existing.getCreatedAt() : now)
                .updatedAt(now)
                .items(items)
                .build();
        return peerVisitPort.save(visit);
    }

    private static void assertWriter(List<String> roles) {
        boolean allowed = roles != null && roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .anyMatch(WRITERS::contains);
        if (!allowed) {
            throw new InvalidRoleException("Solo [TD] o [JD] pueden programar la visita de pares.");
        }
    }
}
