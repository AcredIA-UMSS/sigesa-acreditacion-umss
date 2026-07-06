package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListUsersUseCase {

    record UserSummary(UUID userId, String email, String role, String status, List<UUID> programIds) {}

    List<UserSummary> list(String roleFilter, String statusFilter);
}
