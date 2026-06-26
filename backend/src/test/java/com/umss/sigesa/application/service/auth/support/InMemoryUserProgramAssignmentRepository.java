package com.umss.sigesa.application.service.auth.support;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateActiveAssignmentException;
import com.umss.sigesa.domain.model.UserProgramAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryUserProgramAssignmentRepository implements UserProgramAssignmentRepositoryPort {

    private final List<UserProgramAssignment> assignments = new ArrayList<>();

    @Override
    public UserProgramAssignment save(UserProgramAssignment assignment) {
        if (assignment.isActive()) {
            boolean duplicate = assignments.stream()
                    .anyMatch(a -> a.isActive()
                            && a.getUserId().equals(assignment.getUserId())
                            && a.getProgramId().equals(assignment.getProgramId()));
            if (duplicate) {
                throw new DuplicateActiveAssignmentException(assignment.getUserId(), assignment.getProgramId());
            }
        }
        assignments.add(assignment);
        return assignment;
    }

    @Override
    public List<UserProgramAssignment> findActiveByUserId(UUID userId) {
        return assignments.stream()
                .filter(a -> a.getUserId().equals(userId) && a.isActive())
                .toList();
    }

    @Override
    public void revokeAllActiveByUserId(UUID userId) {
        assignments.stream()
                .filter(a -> a.getUserId().equals(userId) && a.isActive())
                .forEach(UserProgramAssignment::revoke);
    }

    public List<UserProgramAssignment> allAssignments() {
        return List.copyOf(assignments);
    }
}
