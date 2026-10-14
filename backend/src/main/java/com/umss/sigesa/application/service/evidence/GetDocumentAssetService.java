package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.GetDocumentAssetUseCase;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
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

    public GetDocumentAssetService(
            DocumentAssetPort documentAssetPort,
            ProcessQueryPort processQueryPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.documentAssetPort = documentAssetPort;
        this.processQueryPort = processQueryPort;
        this.assignmentRepository = assignmentRepository;
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
}
