package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.FreezeProcessUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.AlreadyFrozenException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FreezeProcessService implements FreezeProcessUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final ProcessQueryPort processQueryPort;
    private final ProcessFreezePort freezePort;
    private final PeerRoomChecklistPort checklistPort;
    private final MethodologicalStagePort stagePort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public FreezeProcessService(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            PeerRoomChecklistPort checklistPort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.freezePort = freezePort;
        this.checklistPort = checklistPort;
        this.stagePort = stagePort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ProcessFreeze freeze(UUID processId, UUID actorId, List<String> roles, List<UUID> jwtProgramScope) {
        assertTd(roles);
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), actorId, roles, jwtProgramScope, assignmentRepository);

        ProcessFreeze existing = freezePort.findByProcessId(processId).orElse(null);
        if (existing != null && existing.isActiveAuditReadonly()) {
            throw new AlreadyFrozenException();
        }

        LocalDateTime now = LocalDateTime.now();
        ProcessFreeze freeze = existing != null ? existing : ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .build();
        freeze.setMode(ProcessFreezeMode.AUDIT_READONLY);
        freeze.setFrozenAt(now);
        freeze.setFrozenBy(actorId);
        freeze.setLiftedAt(null);
        freeze.setLiftedBy(null);

        ProcessFreeze saved = freezePort.save(freeze);
        stagePort.updateProcessOperationalMode(processId, OperationalMode.AUDIT_READONLY);
        seedChecklist(processId, now);
        return saved;
    }

    private void seedChecklist(UUID processId, LocalDateTime now) {
        Set<String> existingCodes = checklistPort.findByProcessId(processId).stream()
                .map(PeerRoomChecklistItem::getItemCode)
                .collect(Collectors.toSet());
        for (PeerRoomChecklistItem.CatalogEntry catalog : PeerRoomChecklistItem.CATALOG) {
            if (!existingCodes.contains(catalog.itemCode())) {
                checklistPort.save(PeerRoomChecklistItem.seed(processId, catalog, now));
            }
        }
    }

    private static void assertTd(List<String> roles) {
        String role = roles == null || roles.isEmpty() ? "" : roles.getFirst().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new InvalidRoleException("Solo [TD] puede congelar el proceso.");
        }
    }
}
