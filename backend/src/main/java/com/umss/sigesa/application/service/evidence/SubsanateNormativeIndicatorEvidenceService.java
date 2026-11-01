package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.SubsanateNormativeIndicatorEvidenceUseCase;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.EvidenceSubsanationResult;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceSubsanationCommand;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class SubsanateNormativeIndicatorEvidenceService implements SubsanateNormativeIndicatorEvidenceUseCase {

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

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ContentHashPort contentHashPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public SubsanateNormativeIndicatorEvidenceService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            NormativeIndicatorWorkflowPort workflowPort,
            EvidenceUploadPersistencePort uploadPersistence,
            EvidenceBlobStoragePort blobStorage,
            ContentHashPort contentHashPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.observationPort = observationPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.workflowPort = workflowPort;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.contentHashPort = contentHashPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public EvidenceSubsanationResult subsanate(NormativeIndicatorEvidenceSubsanationCommand command) {
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(command.indicatorId())
                .orElseThrow(() -> new IndicatorNotFoundException(command.indicatorId()));

        assertProgramScope(command.uploadedBy(), context.careerId());

        IndicatorObservation openObservation = observationPort
                .findLatestOpenByIndicatorId(command.indicatorId())
                .orElseThrow(() -> new SubsanationNotAllowedException(
                        "No hay observación pendiente para subsanar."));

        if (!openObservation.getId().equals(command.observationId())) {
            throw new SubsanationNotAllowedException(
                    "Debe subsanar la observación pendiente más reciente.");
        }

        NormativeIndicatorEvidenceQueryPort.NormativeIndicatorEvidenceRef evidenceRef = evidenceQueryPort
                .findEvidenceRef(command.evidenceId(), command.indicatorId())
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
                null,
                command.description(),
                storageKey,
                command.uploadedBy(),
                now);

        String purgedKey = null;
        try {
            purgedKey = uploadPersistence.persistNormativeIndicatorSubsanation(
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

        if (context.status() == IndicatorState.OBSERVADO) {
            workflowPort.updateIndicatorStatus(command.indicatorId(), IndicatorState.SUBSANADO);
        }

        return new EvidenceSubsanationResult(
                command.evidenceId(),
                nextVersion,
                openObservation.getId(),
                evidenceRef.currentVersionNumber(),
                hash,
                EVENT_EVIDENCE_SUBSANATED);
    }

    private void validateMetadata(NormativeIndicatorEvidenceSubsanationCommand command) {
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
