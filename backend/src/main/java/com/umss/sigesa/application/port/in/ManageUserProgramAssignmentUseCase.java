package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface ManageUserProgramAssignmentUseCase {

    AssignmentResult assign(AssignCommand command);

    record AssignCommand(UUID userId, UUID programId, String action) {
    }

    record AssignmentResult(UUID userId, UUID programId, String action, int revokedCount) {
    }
}
