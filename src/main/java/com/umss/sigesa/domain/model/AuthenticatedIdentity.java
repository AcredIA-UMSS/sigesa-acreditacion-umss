package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record AuthenticatedIdentity(
        UUID userId,
        Email email,
        Role role,
        List<UUID> programScope
) {
}
