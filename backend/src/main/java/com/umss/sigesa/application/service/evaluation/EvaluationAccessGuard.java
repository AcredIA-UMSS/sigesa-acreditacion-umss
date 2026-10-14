package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.process.ProcessAccessPolicy;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;

import java.util.List;
import java.util.UUID;

final class EvaluationAccessGuard {

    private EvaluationAccessGuard() {
    }

    static void assertProcessVisible(
            UUID processId,
            UUID careerId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        String role = primaryRole(roles);
        if (ProcessAccessPolicy.canAccess(role, careerId, jwtProgramScope)) {
            return;
        }
        if ("CC".equals(role) && hasAssignment(requesterId, careerId, assignmentRepository)) {
            return;
        }
        throw new ProcessNotFoundException(processId);
    }

    static String primaryRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.getFirst();
    }

    private static boolean hasAssignment(
            UUID requesterId,
            UUID careerId,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return assignmentRepository.findActiveByUserId(requesterId).stream()
                .anyMatch(assignment -> assignment.getProgramId().equals(careerId));
    }
}
