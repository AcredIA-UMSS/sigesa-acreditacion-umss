package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.UserProgramAssignment;

import java.util.List;
import java.util.UUID;

public interface UserProgramAssignmentRepositoryPort {

    UserProgramAssignment save(UserProgramAssignment assignment);

    List<UserProgramAssignment> findActiveByUserId(UUID userId);

    void revokeAllActiveByUserId(UUID userId);
}
