package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.LiftProcessFreezeUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.FreezeNotFoundException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class LiftProcessFreezeService implements LiftProcessFreezeUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final ProcessQueryPort processQueryPort;
    private final ProcessFreezePort freezePort;
    private final MethodologicalStagePort stagePort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public LiftProcessFreezeService(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.freezePort = freezePort;
        this.stagePort = stagePort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ProcessFreeze lift(
            UUID processId,
            String targetMode,
            UUID actorId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        assertTd(roles);
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), actorId, roles, jwtProgramScope, assignmentRepository);

        ProcessFreeze freeze = freezePort.findByProcessId(processId)
                .orElseThrow(() -> new FreezeNotFoundException(processId));

        OperationalMode operationalMode = parseTargetMode(targetMode);
        freeze.setLiftedAt(LocalDateTime.now());
        freeze.setLiftedBy(actorId);
        if (operationalMode == OperationalMode.IMPROVEMENT_MONITORING) {
            freeze.setMode(ProcessFreezeMode.IMPROVEMENT_PARTIAL);
        }
        ProcessFreeze saved = freezePort.save(freeze);
        stagePort.updateProcessOperationalMode(processId, operationalMode);
        return saved;
    }

    private static OperationalMode parseTargetMode(String targetMode) {
        if (targetMode == null || targetMode.isBlank()) {
            throw new ValidationException("targetMode es obligatorio (ACTIVE o IMPROVEMENT_MONITORING).");
        }
        try {
            OperationalMode mode = OperationalMode.valueOf(targetMode.trim().toUpperCase(Locale.ROOT));
            if (mode == OperationalMode.ACTIVE || mode == OperationalMode.IMPROVEMENT_MONITORING) {
                return mode;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        throw new ValidationException("targetMode debe ser ACTIVE o IMPROVEMENT_MONITORING.");
    }

    private static void assertTd(List<String> roles) {
        String role = roles == null || roles.isEmpty() ? "" : roles.getFirst().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new InvalidRoleException("Solo [TD] puede levantar el freeze.");
        }
    }
}
