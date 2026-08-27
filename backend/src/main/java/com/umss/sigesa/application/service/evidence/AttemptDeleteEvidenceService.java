package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.AttemptDeleteEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceImmutableException;

import java.util.List;
import java.util.UUID;

public class AttemptDeleteEvidenceService implements AttemptDeleteEvidenceUseCase {

    private final EvidenceLifecycleQueryPort lifecycleQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;

    public AttemptDeleteEvidenceService(EvidenceLifecycleQueryPort lifecycleQueryPort,
                                        UserProgramAssignmentRepositoryPort assignmentRepository,
                                        AuditLogPort auditLogPort) {
        this.lifecycleQueryPort = lifecycleQueryPort;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public void attemptDelete(UUID evidenceId, UUID actorId, List<String> roles) {
        EvidenceLifecycleQueryPort.EvidenceContext context =
                EvidenceLifecycleAccessGuard.requireContext(lifecycleQueryPort, evidenceId);
        EvidenceLifecycleAccessGuard.assertReadAccess(
                actorId, roles, context.programId(), assignmentRepository);
        auditLogPort.logDeleteDenied(actorId, evidenceId);
        throw new EvidenceImmutableException();
    }
}
