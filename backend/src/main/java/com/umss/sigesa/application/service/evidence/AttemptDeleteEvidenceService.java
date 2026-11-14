package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.AttemptDeleteEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.CaeDelegationGuard;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.EvidenceImmutableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class AttemptDeleteEvidenceService implements AttemptDeleteEvidenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(AttemptDeleteEvidenceService.class);

    private final EvidenceLifecycleQueryPort lifecycleQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ProcessFreezeGuard freezeGuard;
    private final CaeDelegationGuard caeDelegationGuard;

    public AttemptDeleteEvidenceService(EvidenceLifecycleQueryPort lifecycleQueryPort,
                                        UserProgramAssignmentRepositoryPort assignmentRepository,
                                        AuditLogPort auditLogPort,
                                        EvidenceUploadPersistencePort uploadPersistence,
                                        EvidenceBlobStoragePort blobStorage,
                                        ProcessFreezeGuard freezeGuard,
                                        CaeDelegationGuard caeDelegationGuard) {
        this.lifecycleQueryPort = lifecycleQueryPort;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.freezeGuard = freezeGuard;
        this.caeDelegationGuard = caeDelegationGuard;
    }

    @Override
    public void attemptDelete(UUID evidenceId, UUID actorId, List<String> roles, List<UUID> jwtProgramScope) {
        EvidenceLifecycleQueryPort.EvidenceContext context =
                EvidenceLifecycleAccessGuard.requireContext(lifecycleQueryPort, evidenceId);

        if (!context.canRetract()) {
            EvidenceLifecycleAccessGuard.assertReadAccess(
                    actorId, roles, context.programId(), assignmentRepository);
            auditLogPort.logDeleteDenied(actorId, evidenceId);
            throw new EvidenceImmutableException();
        }

        EvidenceLifecycleAccessGuard.assertWriteAccess(
                actorId, roles, context.programId(), jwtProgramScope, assignmentRepository);

        if (context.processId() != null) {
            freezeGuard.assertWritable(context.processId());
            caeDelegationGuard.assertCanWrite(actorId, context.processId(), context.normativeIndicatorId());
        }

        List<String> blobKeys = uploadPersistence.retractNormativeEvidence(evidenceId);
        for (String key : blobKeys) {
            try {
                blobStorage.delete(key);
            } catch (RuntimeException ex) {
                log.warn("No se pudo borrar blob {} al retirar evidencia {}: {}", key, evidenceId, ex.getMessage());
            }
        }
        auditLogPort.logEvidenceRetracted(actorId, evidenceId, context.normativeIndicatorId());
    }
}
