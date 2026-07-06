package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.List;
import java.util.UUID;

public class ListUsersService implements ListUsersUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListUsersService(UserRepositoryPort userRepository,
                            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<UserSummary> list(String roleFilter, String statusFilter) {
        Role role = parseRole(roleFilter);
        UserStatus status = parseStatus(statusFilter);

        return userRepository.findAllFiltered(role, status).stream()
                .map(this::toSummary)
                .toList();
    }

    private UserSummary toSummary(AppUser user) {
        List<UUID> programIds = assignmentRepository.findActiveByUserId(user.getId()).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();

        return new UserSummary(
                user.getId(),
                user.getEmail().value(),
                user.getRole().name(),
                user.getStatus().name(),
                programIds
        );
    }

    private Role parseRole(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(roleFilter.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException("Rol de filtro inválido: " + roleFilter);
        }
    }

    private UserStatus parseStatus(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return null;
        }
        try {
            return UserStatus.valueOf(statusFilter.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidFilterException(
                    "Estado de filtro inválido: " + statusFilter + ". Valores permitidos: INACTIVE, ACTIVE, DEACTIVATED.");
        }
    }
}
