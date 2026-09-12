package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.UploadNormativeIndicatorEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidencePayloadTooLargeException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidEvidenceFormatException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UploadNormativeIndicatorEvidenceService implements UploadNormativeIndicatorEvidenceUseCase {

    public static final String EVENT_EVIDENCE_UPLOADED = "EvidenceUploaded";
    private static final long MAX_BYTES = 50L * 1024 * 1024;
    private static final String EXTERNAL_STORAGE_PREFIX = "external-url:";

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/png",
            "image/jpeg"
    );

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.ofEntries(
            Map.entry(".pdf", "application/pdf"),
            Map.entry(".doc", "application/msword"),
            Map.entry(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry(".xls", "application/vnd.ms-excel"),
            Map.entry(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry(".png", "image/png"),
            Map.entry(".jpg", "image/jpeg"),
            Map.entry(".jpeg", "image/jpeg")
    );

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final EvidenceBlobStoragePort blobStorage;
    private final ContentHashPort contentHashPort;
    private final NotificationOutboxPort notificationOutbox;
    private final AuditLogPort auditLogPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UploadNormativeIndicatorEvidenceService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorWorkflowPort workflowPort,
            EvidenceUploadPersistencePort uploadPersistence,
            EvidenceBlobStoragePort blobStorage,
            ContentHashPort contentHashPort,
            NotificationOutboxPort notificationOutbox,
            AuditLogPort auditLogPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.observationPort = observationPort;
        this.workflowPort = workflowPort;
        this.uploadPersistence = uploadPersistence;
        this.blobStorage = blobStorage;
        this.contentHashPort = contentHashPort;
        this.notificationOutbox = notificationOutbox;
        this.auditLogPort = auditLogPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public NormativeIndicatorEvidenceUploadResult upload(NormativeIndicatorEvidenceUploadCommand command) {
        validateMetadata(command);

        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(command.indicatorId())
                .orElseThrow(() -> new IndicatorNotFoundException(command.indicatorId()));

        observationPort.findLatestOpenByIndicatorId(command.indicatorId()).ifPresent(open -> {
            throw new SubsanationNotAllowedException(
                    "Hay una observación pendiente; subsane la evidencia en lugar de cargar una nueva.");
        });

        assertProgramScope(command.uploadedBy(), context.careerId());

        IndicatorState resultingState = context.status();
        if (context.status() == IndicatorState.PENDIENTE) {
            workflowPort.updateIndicatorStatus(command.indicatorId(), IndicatorState.SUBIDO);
            resultingState = IndicatorState.SUBIDO;
        }

        UUID evidenceId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        boolean externalOnly = isExternalOnlyUpload(command);
        String hash;
        String storageKey;
        String externalUrl = normalizeExternalUrl(command.externalUrl());

        if (externalOnly) {
            hash = contentHashPort.sha256Hex(externalUrl.getBytes(StandardCharsets.UTF_8));
            storageKey = EXTERNAL_STORAGE_PREFIX + evidenceId;
        } else {
            validatePayload(command.fileContent(), command.contentType(), command.originalFilename());
            hash = contentHashPort.sha256Hex(command.fileContent());
            storageKey = blobStorage.store(
                    evidenceId, 1, command.fileContent(), command.originalFilename());
        }

        try {
            Evidence evidence = Evidence.forNormativeIndicator(evidenceId, command.indicatorId(), versionId, now);
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

            uploadPersistence.persistNormativeIndicatorUpload(evidence, version, externalUrl);

            notificationOutbox.enqueueEvidenceUploaded(
                    command.indicatorId(), evidenceId, context.careerId());
            auditLogPort.logEvidenceUploaded(command.uploadedBy(), evidenceId, command.indicatorId());

            return new NormativeIndicatorEvidenceUploadResult(
                    evidenceId,
                    1,
                    hash,
                    EVENT_EVIDENCE_UPLOADED,
                    resultingState);
        } catch (RuntimeException ex) {
            if (!externalOnly) {
                blobStorage.delete(storageKey);
            }
            throw ex;
        }
    }

    private void validateMetadata(NormativeIndicatorEvidenceUploadCommand command) {
        if (command.description() == null || command.description().isBlank()) {
            throw new EvidenceUnclassifiedException("description");
        }
        boolean hasFile = command.fileContent() != null && command.fileContent().length > 0;
        boolean hasExternalUrl = command.externalUrl() != null && !command.externalUrl().isBlank();
        if (!hasFile && !hasExternalUrl) {
            throw new EvidenceUnclassifiedException("file or externalUrl");
        }
        if (hasExternalUrl) {
            validateExternalUrl(command.externalUrl());
        }
    }

    private static boolean isExternalOnlyUpload(NormativeIndicatorEvidenceUploadCommand command) {
        boolean hasFile = command.fileContent() != null && command.fileContent().length > 0;
        boolean hasExternalUrl = command.externalUrl() != null && !command.externalUrl().isBlank();
        return hasExternalUrl && !hasFile;
    }

    private static String normalizeExternalUrl(String externalUrl) {
        if (externalUrl == null || externalUrl.isBlank()) {
            return null;
        }
        return externalUrl.trim();
    }

    private static void validateExternalUrl(String externalUrl) {
        try {
            URI uri = URI.create(externalUrl.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidEvidenceFormatException("externalUrl must be an absolute http(s) URL");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new InvalidEvidenceFormatException("externalUrl must use http or https");
            }
        } catch (IllegalArgumentException ex) {
            throw new InvalidEvidenceFormatException("Invalid externalUrl: " + externalUrl);
        }
    }

    private void validatePayload(byte[] content, String contentType, String originalFilename) {
        if (content.length > MAX_BYTES) {
            throw new EvidencePayloadTooLargeException(MAX_BYTES);
        }
        String resolved = resolveContentType(contentType, originalFilename);
        if (!ALLOWED_CONTENT_TYPES.contains(resolved)) {
            throw new InvalidEvidenceFormatException("Unsupported content type: " + contentType);
        }
    }

    static String resolveContentType(String contentType, String originalFilename) {
        String normalized = contentType != null ? normalizeContentType(contentType) : "";
        if (ALLOWED_CONTENT_TYPES.contains(normalized)) {
            return normalized;
        }
        if ("application/octet-stream".equals(normalized) || normalized.isEmpty()) {
            return inferContentTypeFromFilename(originalFilename).orElse(normalized);
        }
        return normalized;
    }

    private static Optional<String> inferContentTypeFromFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return Optional.empty();
        }
        String lower = originalFilename.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : EXTENSION_TO_CONTENT_TYPE.entrySet()) {
            if (lower.endsWith(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
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
