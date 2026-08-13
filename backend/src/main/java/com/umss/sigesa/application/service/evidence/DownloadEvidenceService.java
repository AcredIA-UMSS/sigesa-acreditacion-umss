package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.application.port.in.DownloadEvidenceUseCase;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;

import java.util.List;
import java.util.UUID;

public class DownloadEvidenceService implements DownloadEvidenceUseCase {

    private final SearchEvidenceQueryPort queryPort;
    private final EvidenceBlobStoragePort blobStorage;

    public DownloadEvidenceService(SearchEvidenceQueryPort queryPort, EvidenceBlobStoragePort blobStorage) {
        this.queryPort = queryPort;
        this.blobStorage = blobStorage;
    }

    @Override
    public EvidenceFileResult download(UUID versionId, UUID userId, String role, List<UUID> programScope) {
        List<UUID> effectiveScope = "CC".equalsIgnoreCase(role) ? programScope : null;

        EvidenceSearchDetailDto detail = queryPort.findVersionById(versionId, effectiveScope)
                .orElseThrow(() -> new EvidenceNotFoundException(versionId));

        byte[] content = blobStorage.retrieve(detail.title());

        // Deducir contentType basado en la extensión de storageKey
        String contentType = detectContentType(detail.title());

        return new EvidenceFileResult(detail.title(), contentType, content);
    }

    private String detectContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if (lower.endsWith(".zip")) {
            return "application/zip";
        }
        return "application/octet-stream";
    }
}
