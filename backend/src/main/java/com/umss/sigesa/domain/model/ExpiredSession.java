package com.umss.sigesa.domain.model;

import java.util.UUID;

public record ExpiredSession(UUID userId, String email) {
}
