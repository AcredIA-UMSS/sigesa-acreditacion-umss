package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorNotUploadableException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.UploadInProgressException;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceUploadCommand;
import com.umss.sigesa.domain.model.EvidenceUploadResult;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import com.umss.sigesa.domain.model.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UploadEvidenceService implements UploadEvidenceUseCase {

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

    private final IndicatorRepositoryPort indicatorRepository;
    private final EvidenceRepositoryPort evidenceRepository;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ContentHashPort contentHashPort;
    private final EvidenceUploadLockPort uploadLock;
    private final NotificationOutboxPort notificationOutbox;
    private final AuditLogPort auditLogPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UploadEvidenceService(IndicatorRepositoryPort indicatorRepository,
                                 EvidenceRepositoryPort evidenceRepository,
                                 EvidenceUploadPersistencePort uploadPersistence,
                                 EvidenceBlobStoragePort blobStorage,
                                 ContentHashPort contentHashPort,
                                 EvidenceUploadLockPort uploadLock,
                                 NotificationOutboxPort notificationOutbox,
                                 AuditLogPort auditLogPort,
                                 UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.indicatorRepository = indicatorRepository;
        this.evidenceRepository = evidenceRepository;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.contentHashPort = contentHashPort;
        this.uploadLock = uploadLock;
        this.notificationOutbox = notificationOutbox;
        this.auditLogPort = auditLogPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public EvidenceUploadResult upload(EvidenceUploadCommand command) {
        validateMetadata(command);
        validatePayload(command.fileContent(), command.contentType());

        Indicator indicator = indicatorRepository.findById(command.indicatorId())
                .orElseThrow(() -> new IndicatorNotFoundException(command.indicatorId()));

        validateCriterionMatchesIndicator(command.criterionId(), indicator);
        assertProgramScope(command.uploadedBy(), indicator.getProgramId());

        IndicatorState currentState = indicatorRepository.getCurrentState(command.indicatorId());
        validateUploadableState(currentState, command.indicatorId());

        if (!uploadLock.tryAcquire(command.indicatorId())) {
            throw new UploadInProgressException();
        }

        String storageKey = null;
        try {
            UUID evidenceId = UUID.randomUUID();
            UUID versionId = UUID.randomUUID();
            String hash = contentHashPort.sha256Hex(command.fileContent());
            storageKey = blobStorage.store(
                    evidenceId, 1, command.fileContent(), command.originalFilename());

            LocalDateTime now = LocalDateTime.now();
            Evidence evidence = new Evidence(evidenceId, command.indicatorId(), versionId, now);
            EvidenceVersion version = new EvidenceVersion(
                    versionId,
                    evidenceId,
                    1,
                    hash,
                    command.criterionId(),
                    command.description(),
                    storageKey,
                    command.uploadedBy(),
                    now
            );
            IndicatorStateHistoryEntry historyEntry = new IndicatorStateHistoryEntry(
                    UUID.randomUUID(),
                    command.indicatorId(),
                    currentState,
                    IndicatorState.SUBIDO,
                    command.uploadedBy(),
                    Role.CC,
                    now
            );

            try {
                uploadPersistence.persistUpload(evidence, version, historyEntry);
            } catch (RuntimeException ex) {
                blobStorage.delete(storageKey);
                storageKey = null;
                throw ex;
            }

            notificationOutbox.enqueueEvidenceUploaded(
                    command.indicatorId(), evidenceId, indicator.getProgramId());
            auditLogPort.logEvidenceUploaded(command.uploadedBy(), evidenceId, command.indicatorId());

            return new EvidenceUploadResult(
                    evidenceId,
                    1,
                    hash,
                    EVENT_EVIDENCE_UPLOADED,
                    IndicatorState.SUBIDO
            );
        } finally {
            uploadLock.release(command.indicatorId());
        }
    }

    private void validateMetadata(EvidenceUploadCommand command) {
        if (command.criterionId() == null) {
            throw new EvidenceUnclassifiedException("criterionId");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new EvidenceUnclassifiedException("description");
        }
        if (command.fileContent() == null || command.fileContent().length == 0) {
            throw new EvidenceUnclassifiedException("file");
        }
    }

    private void validateCriterionMatchesIndicator(UUID criterionId, Indicator indicator) {
        if (!criterionId.equals(indicator.getCriterionId())) {
            throw new EvidenceUnclassifiedException("criterionId does not match indicator taxonomy");
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

    private void validateUploadableState(IndicatorState state, UUID indicatorId) {
        if (state != IndicatorState.PENDIENTE && state != IndicatorState.OBSERVADO) {
            throw new IndicatorNotUploadableException(
                    "Indicator state " + state + " does not allow initial upload");
        }
        if (evidenceRepository.existsByIndicatorId(indicatorId)) {
            throw new IndicatorNotUploadableException(
                    "Evidence already exists; use subsanation endpoint (UC-006)");
        }
    }
}
