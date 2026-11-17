package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.ListNormativeIndicatorEvidencesUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceItem;

import java.util.List;
import java.util.UUID;

public class ListNormativeIndicatorEvidencesService implements ListNormativeIndicatorEvidencesUseCase {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListNormativeIndicatorEvidencesService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<NormativeIndicatorEvidenceItem> list(UUID indicatorId, UUID requesterId, List<String> roles) {
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        assertAccess(context.careerId(), requesterId, roles);
        return evidenceQueryPort.listByIndicatorId(indicatorId);
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
