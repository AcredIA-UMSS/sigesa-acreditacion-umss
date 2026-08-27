package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.UploadSubphaseEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.SubphaseTransitionHelper;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceUploadResult;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.SubphaseEvidenceUploadCommand;
import com.umss.sigesa.domain.model.SubphaseState;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class UploadSubphaseEvidenceService implements UploadSubphaseEvidenceUseCase {

    public static final String EVENT_EVIDENCE_UPLOADED = "EvidenceUploaded";
    private static final long MAX_BYTES = 50L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/png",
            "image/jpeg"
    );

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseObservationPort observationPort;
    private final SubphaseWorkflowPort subphaseWorkflowPort;
    private final SubphaseTransitionHelper transitionHelper;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ContentHashPort contentHashPort;
    private final NotificationOutboxPort notificationOutbox;
    private final AuditLogPort auditLogPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UploadSubphaseEvidenceService(SubphaseQueryPort subphaseQueryPort,
                                         SubphaseObservationPort observationPort,
                                         SubphaseWorkflowPort subphaseWorkflowPort,
                                         SubphaseTransitionHelper transitionHelper,
                                         EvidenceUploadPersistencePort uploadPersistence,
                                         EvidenceBlobStoragePort blobStorage,
                                         ContentHashPort contentHashPort,
                                         NotificationOutboxPort notificationOutbox,
                                         AuditLogPort auditLogPort,
                                         UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.observationPort = observationPort;
        this.subphaseWorkflowPort = subphaseWorkflowPort;
        this.transitionHelper = transitionHelper;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.contentHashPort = contentHashPort;
        this.notificationOutbox = notificationOutbox;
        this.auditLogPort = auditLogPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public EvidenceUploadResult upload(SubphaseEvidenceUploadCommand command) {
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(command.subphaseId())
                .orElseThrow(() -> new ProcessNotFoundException(
                        "Subfase no encontrada: " + command.subphaseId()));

        observationPort.findLatestOpenBySubphaseId(command.subphaseId()).ifPresent(open -> {
            throw new SubsanationNotAllowedException(
                    "Hay una observación pendiente; subsane la evidencia en lugar de cargar una nueva.");
        });

        validateMetadata(command);
        validatePayload(command.fileContent(), command.contentType());
        assertProgramScope(command.uploadedBy(), context.careerId());

        SubphaseState currentState = subphaseWorkflowPort.getCurrentState(command.subphaseId());
        SubphaseState resultingState = currentState;
        if (currentState == SubphaseState.PENDIENTE) {
            resultingState = transitionHelper.transition(
                    command.subphaseId(),
                    SubphaseState.SUBIDO,
                    EnumSet.of(SubphaseState.PENDIENTE)).newState();
        }

        UUID evidenceId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        String hash = contentHashPort.sha256Hex(command.fileContent());
        String storageKey = blobStorage.store(
                evidenceId, 1, command.fileContent(), command.originalFilename());
        LocalDateTime now = LocalDateTime.now();

        try {
            Evidence evidence = new Evidence(
                    evidenceId,
                    null,
                    command.subphaseId(),
                    versionId,
                    now);
            EvidenceVersion version = new EvidenceVersion(
                    versionId,
                    evidenceId,
                    1,
                    hash,
                    null,
                    command.description(),
                    storageKey,
                    command.uploadedBy(),
                    now);

            uploadPersistence.persistSubphaseUpload(evidence, version);

            notificationOutbox.enqueueEvidenceUploaded(
                    command.subphaseId(), evidenceId, context.careerId());
            auditLogPort.logEvidenceUploaded(command.uploadedBy(), evidenceId, command.subphaseId());

            return new EvidenceUploadResult(
                    evidenceId,
                    1,
                    hash,
                    EVENT_EVIDENCE_UPLOADED,
                    resultingState);
        } catch (RuntimeException ex) {
            blobStorage.delete(storageKey);
            throw ex;
        }
    }

    private void validateMetadata(SubphaseEvidenceUploadCommand command) {
        if (command.description() == null || command.description().isBlank()) {
            throw new EvidenceUnclassifiedException("description");
        }
        if (command.fileContent() == null || command.fileContent().length == 0) {
            throw new EvidenceUnclassifiedException("file");
        }
    }

    private void validatePayload(byte[] content, String contentType) {
        if (content.length > MAX_BYTES) {
            throw new EvidencePayloadTooLargeException(MAX_BYTES);
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(normalizeContentType(contentType))) {
            throw new InvalidEvidenceFormatException("Unsupported content type: " + contentType);
        }
    }

    private static String normalizeContentType(String contentType) {
        return contentType.split(";")[0].trim().toLowerCase();
    }

    private void assertProgramScope(UUID userId, UUID programId) {
        boolean allowed = assignmentRepository.findActiveByUserId(userId).stream()
                .anyMatch(a -> a.getProgramId().equals(programId));
        if (!allowed) {
            throw new ProgramScopeDeniedException();
        }
    }
}
