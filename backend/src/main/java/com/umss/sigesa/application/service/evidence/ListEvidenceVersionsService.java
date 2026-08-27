package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.ListEvidenceVersionsUseCase;
import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;

import java.util.List;
import java.util.UUID;

public class ListEvidenceVersionsService implements ListEvidenceVersionsUseCase {

    private final EvidenceLifecycleQueryPort lifecycleQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListEvidenceVersionsService(EvidenceLifecycleQueryPort lifecycleQueryPort,
                                       UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.lifecycleQueryPort = lifecycleQueryPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<EvidenceVersionHistoryItem> list(UUID evidenceId, UUID userId, List<String> roles) {
        EvidenceLifecycleQueryPort.EvidenceContext context =
                EvidenceLifecycleAccessGuard.requireContext(lifecycleQueryPort, evidenceId);
        EvidenceLifecycleAccessGuard.assertReadAccess(
                userId, roles, context.programId(), assignmentRepository);
        return lifecycleQueryPort.listVersions(evidenceId, context.latestVersionId());
    }
}
