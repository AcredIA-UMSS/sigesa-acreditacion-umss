package com.umss.sigesa.application.service;

import com.umss.sigesa.adapter.in.web.dto.EvidenceResponse;
import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.FileStoragePort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.exception.InvalidFileFormatException;
import com.umss.sigesa.domain.exception.MaxFileSizeExceededException;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UploadEvidenceService implements UploadEvidenceUseCase {

    private final EvidenceRepositoryPort evidenceRepository;
    private final FileStoragePort fileStorage;
    private final IndicatorStateHistoryPort indicatorStateHistory;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
            "application/vnd.ms-excel", // xls
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
            "application/msword" // doc
    );

    public UploadEvidenceService(EvidenceRepositoryPort evidenceRepository,
                                 FileStoragePort fileStorage,
                                 IndicatorStateHistoryPort indicatorStateHistory) {
        this.evidenceRepository = evidenceRepository;
        this.fileStorage = fileStorage;
        this.indicatorStateHistory = indicatorStateHistory;
    }

    @Override
    public EvidenceResponse upload(UUID indicatorId, UUID criterionId, String description,
                                   String filename, byte[] fileBytes, String contentType,
                                   AuthenticatedIdentity identity) {
        if (indicatorId == null || criterionId == null) {
            throw new EvidenceUnclassifiedException("La evidencia debe estar asociada a un indicador y criterio válidos.");
        }

        if (identity.role() != Role.CC) {
            throw new ForbiddenProgramScopeException("Solo el Coordinador de Carrera [CC] está autorizado para cargar evidencias.");
        }

        UUID indicatorProgramId = evidenceRepository.findProgramIdForIndicator(indicatorId);
        if (!identity.programScope().contains(indicatorProgramId)) {
            throw new ForbiddenProgramScopeException("El usuario no tiene permisos sobre la carrera del indicador especificado.");
        }

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        if (fileBytes.length > MAX_FILE_SIZE) {
            throw new MaxFileSizeExceededException("El archivo supera el límite de tamaño permitido de 5MB.");
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileFormatException("El formato de archivo '" + contentType + "' no está permitido.");
        }

        FileStoragePort.StorageResult storageResult = fileStorage.store(filename, fileBytes);

        UUID evidenceId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Evidence evidence = new Evidence(evidenceId, indicatorId, versionId, now);
        EvidenceVersion version = new EvidenceVersion(
                versionId,
                evidenceId,
                1,
                description,
                storageResult.storageKey(),
                storageResult.contentHash(),
                null,
                identity.userId(),
                now
        );

        evidenceRepository.save(evidence, version);

        indicatorStateHistory.recordTransition(indicatorId, "PENDIENTE", "SUBIDO", identity.userId(), identity.role());

        return new EvidenceResponse(evidenceId, 1, storageResult.contentHash(), "EvidenceUploaded");
    }
}
