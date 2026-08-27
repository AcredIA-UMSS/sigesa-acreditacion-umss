package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.ListSubphaseEvidencesUseCase;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.SubphaseEvidenceItem;

import java.util.List;
import java.util.UUID;

public class ListSubphaseEvidencesService implements ListSubphaseEvidencesUseCase {

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListSubphaseEvidencesService(SubphaseQueryPort subphaseQueryPort,
                                        SubphaseEvidenceQueryPort evidenceQueryPort,
                                        UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<SubphaseEvidenceItem> list(UUID subphaseId, UUID requesterId, List<String> roles) {
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));
        assertAccess(context.careerId(), requesterId, roles);
        return evidenceQueryPort.listBySubphaseId(subphaseId);
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
