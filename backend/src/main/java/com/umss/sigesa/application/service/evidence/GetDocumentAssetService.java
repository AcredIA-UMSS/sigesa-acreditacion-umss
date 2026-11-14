package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.GetDocumentAssetUseCase;
import com.umss.sigesa.application.port.in.GetDocumentAssetUseCase.DocumentAssetBinary;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.exception.EvidenceBlobNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.DocumentAssetView;

import java.util.List;
import java.util.UUID;

public class GetDocumentAssetService implements GetDocumentAssetUseCase {

    private final DocumentAssetPort documentAssetPort;
    private final ProcessQueryPort processQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final EvidenceBlobStoragePort blobStorage;

    public GetDocumentAssetService(
            DocumentAssetPort documentAssetPort,
            ProcessQueryPort processQueryPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            EvidenceBlobStoragePort blobStorage) {
        this.documentAssetPort = documentAssetPort;
        this.processQueryPort = processQueryPort;
        this.assignmentRepository = assignmentRepository;
        this.blobStorage = blobStorage;
    }

    @Override
    public DocumentAssetView get(UUID documentAssetId, UUID requesterId, List<String> roles) {
        DocumentAsset asset = documentAssetPort.findById(documentAssetId)
                .orElseThrow(() -> new DocumentAssetNotFoundException(documentAssetId));
        AccreditationProcess process = processQueryPort.findDetailById(asset.getProcessId())
                .orElseThrow(() -> new ProcessNotFoundException(asset.getProcessId()));
        assertAccess(process.getCareerId(), requesterId, roles);
        return toView(asset, documentAssetPort.findLinkedIndicatorIds(asset.getId()));
    }

    @Override
    public List<DocumentAssetView> listByProcess(UUID processId, UUID requesterId, List<String> roles) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        assertAccess(process.getCareerId(), requesterId, roles);
        return documentAssetPort.findByProcessId(processId).stream()
                .map(asset -> toView(asset, documentAssetPort.findLinkedIndicatorIds(asset.getId())))
                .toList();
    }

    @Override
    public DocumentAssetBinary download(UUID documentAssetId, UUID requesterId, List<String> roles) {
        DocumentAsset asset = documentAssetPort.findById(documentAssetId)
                .orElseThrow(() -> new DocumentAssetNotFoundException(documentAssetId));
        AccreditationProcess process = processQueryPort.findDetailById(asset.getProcessId())
                .orElseThrow(() -> new ProcessNotFoundException(asset.getProcessId()));
        assertAccess(process.getCareerId(), requesterId, roles);

        String key = asset.getStorageKey();
        if (key == null || key.startsWith("external-url:")) {
            throw new EvidenceBlobNotFoundException(documentAssetId);
        }
        byte[] content = blobStorage.retrieve(key)
                .orElseThrow(() -> new EvidenceBlobNotFoundException(documentAssetId));
        String filename = filenameFromKey(key, asset.getTitle());
        return new DocumentAssetBinary(content, filename, mediaType(filename));
    }

    private static DocumentAssetView toView(DocumentAsset asset, List<UUID> linkedIndicatorIds) {
        return new DocumentAssetView(
                asset.getId(),
                asset.getFolioCode(),
                asset.getTitle(),
                asset.getContentHash(),
                asset.getProcessId(),
                List.copyOf(linkedIndicatorIds),
                asset.getCreatedAt());
    }

    private void assertAccess(UUID careerId, UUID requesterId, List<String> roles) {
        if (roles != null && roles.stream().anyMatch(r -> "JD".equals(r) || "TD".equals(r))) {
            return;
        }
        boolean allowed = assignmentRepository.findActiveByUserId(requesterId).stream()
                .anyMatch(a -> a.getProgramId().equals(careerId));
        if (!allowed) {
            throw new ProgramScopeDeniedException();
        }
    }

    private static String filenameFromKey(String storageKey, String title) {
        int slash = storageKey.lastIndexOf('/');
        int underscore = storageKey.lastIndexOf('_');
        if (slash >= 0 && slash < storageKey.length() - 1) {
            return storageKey.substring(slash + 1);
        }
        if (underscore >= 0 && underscore < storageKey.length() - 1) {
            return storageKey.substring(underscore + 1);
        }
        return title == null || title.isBlank() ? "documento" : title;
    }

    private static String mediaType(String filename) {
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
        if (lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}
