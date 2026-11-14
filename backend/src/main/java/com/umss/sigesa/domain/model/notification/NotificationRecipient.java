package com.umss.sigesa.domain.model.notification;

import com.umss.sigesa.domain.model.Role;

import java.util.UUID;

public record NotificationRecipient(UUID userId, String email, Role role) {
}
