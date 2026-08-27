package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.Role;

import java.util.List;
import java.util.UUID;

final class EvidenceLifecycleAccessGuard {

    private EvidenceLifecycleAccessGuard() {
    }

    static EvidenceLifecycleQueryPort.EvidenceContext requireContext(
            EvidenceLifecycleQueryPort queryPort,
            UUID evidenceId) {
        return queryPort.findContext(evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));
    }

    static void assertReadAccess(UUID userId, List<String> roles, UUID programId,
                                   UserProgramAssignmentRepositoryPort assignmentRepository) {
        if (roles.contains(Role.JD.name()) || roles.contains(Role.TD.name())) {
            return;
        }
        if (roles.contains(Role.CC.name())) {
            boolean allowed = assignmentRepository.findActiveByUserId(userId).stream()
                    .anyMatch(a -> a.getProgramId().equals(programId));
            if (!allowed) {
                throw new ProgramScopeDeniedException();
            }
            return;
        }
        throw new ProgramScopeDeniedException();
    }
}
