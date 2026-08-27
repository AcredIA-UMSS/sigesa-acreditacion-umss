package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import com.umss.sigesa.application.port.out.EvidenceSearchQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchPage;

import java.util.List;
import java.util.UUID;

public class SearchEvidencesService implements SearchEvidencesUseCase {

    private final EvidenceSearchQueryPort searchQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public SearchEvidencesService(EvidenceSearchQueryPort searchQueryPort,
                                  UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.searchQueryPort = searchQueryPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public EvidenceSearchPage search(EvidenceSearchCriteria criteria, UUID requesterId, List<String> roles) {
        List<UUID> allowedProgramIds = resolveAllowedProgramIds(requesterId, roles);
        if (allowedProgramIds != null && allowedProgramIds.isEmpty()) {
            throw new ProgramScopeDeniedException();
        }
        return searchQueryPort.search(criteria, allowedProgramIds);
    }

    private List<UUID> resolveAllowedProgramIds(UUID requesterId, List<String> roles) {
        if (roles != null && roles.stream().anyMatch(r -> "JD".equals(r) || "TD".equals(r))) {
            return null;
        }
        return assignmentRepository.findActiveByUserId(requesterId).stream()
                .map(a -> a.getProgramId())
                .distinct()
                .toList();
    }
}
