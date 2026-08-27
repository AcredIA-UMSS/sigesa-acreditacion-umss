package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.SubsanateSubphaseEvidenceUseCase;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.application.service.workflow.SubphaseTransitionHelper;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.SubphaseEvidenceSubsanationCommand;
import com.umss.sigesa.domain.model.SubphaseObservation;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class SubsanateSubphaseEvidenceService implements SubsanateSubphaseEvidenceUseCase {

    public static final String EVENT_EVIDENCE_SUBSANATED = "EvidenceSubsanated";
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
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ContentHashPort contentHashPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final SubphaseTransitionHelper transitionHelper;

    public SubsanateSubphaseEvidenceService(SubphaseQueryPort subphaseQueryPort,
                                            SubphaseObservationPort observationPort,
                                            SubphaseEvidenceQueryPort evidenceQueryPort,
                                            EvidenceUploadPersistencePort uploadPersistence,
                                            EvidenceBlobStoragePort blobStorage,
                                            ContentHashPort contentHashPort,
                                            UserProgramAssignmentRepositoryPort assignmentRepository,
                                            SubphaseTransitionHelper transitionHelper) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.observationPort = observationPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.contentHashPort = contentHashPort;
        this.assignmentRepository = assignmentRepository;
        this.transitionHelper = transitionHelper;
    }

    @Override
    public EvidenceSubsanationResult subsanate(SubphaseEvidenceSubsanationCommand command) {
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(command.subphaseId())
                .orElseThrow(() -> new ProcessNotFoundException(
                        "Subfase no encontrada: " + command.subphaseId()));

        assertProgramScope(command.uploadedBy(), context.careerId());

        SubphaseObservation openObservation = observationPort.findLatestOpenBySubphaseId(command.subphaseId())
                .orElseThrow(() -> new SubsanationNotAllowedException(
                        "No hay observación pendiente para subsanar."));

        if (!openObservation.getId().equals(command.observationId())) {
            throw new SubsanationNotAllowedException(
                    "Debe subsanar la observación pendiente más reciente.");
        }

        SubphaseEvidenceQueryPort.SubphaseEvidenceRef evidenceRef = evidenceQueryPort
                .findEvidenceRef(command.evidenceId(), command.subphaseId())
                .orElseThrow(() -> new EvidenceNotFoundException(command.evidenceId()));

        validateMetadata(command);
        validatePayload(command.fileContent(), command.contentType());

        int nextVersion = evidenceRef.currentVersionNumber() + 1;
        UUID newVersionId = UUID.randomUUID();
        String hash = contentHashPort.sha256Hex(command.fileContent());
        String storageKey = blobStorage.store(
                command.evidenceId(), nextVersion, command.fileContent(), command.originalFilename());
        LocalDateTime now = LocalDateTime.now();

        EvidenceVersion newVersion = new EvidenceVersion(
                newVersionId,
                command.evidenceId(),
                nextVersion,
                hash,
                evidenceRef.criterionId(),
                command.description(),
                storageKey,
                command.uploadedBy(),
                now);

        String purgedKey = null;
        try {
            purgedKey = uploadPersistence.persistSubphaseSubsanation(
                    command.evidenceId(),
                    newVersion,
                    openObservation.getId(),
                    evidenceRef.currentVersionNumber(),
                    evidenceRef.latestVersionId());
        } catch (RuntimeException ex) {
            blobStorage.delete(storageKey);
            throw ex;
        }

        if (purgedKey != null && !purgedKey.isBlank()) {
            blobStorage.delete(purgedKey);
        }

        transitionHelper.transition(
                command.subphaseId(),
                SubphaseState.SUBSANADO,
                EnumSet.of(SubphaseState.OBSERVADO));

        return new EvidenceSubsanationResult(
                command.evidenceId(),
                nextVersion,
                openObservation.getId(),
                evidenceRef.currentVersionNumber(),
                hash,
                EVENT_EVIDENCE_SUBSANATED);
    }

    private void validateMetadata(SubphaseEvidenceSubsanationCommand command) {
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
