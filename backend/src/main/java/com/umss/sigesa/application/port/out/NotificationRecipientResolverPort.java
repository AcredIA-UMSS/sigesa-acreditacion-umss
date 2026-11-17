package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRecipientResolverPort {

    List<NotificationRecipient> resolveForProgram(UUID programId, Role role);

    List<NotificationRecipient> resolveActiveByRole(Role role);

    Optional<NotificationRecipient> resolveProcessResponsible(UUID processId);

    Optional<NotificationRecipient> resolveUser(UUID userId);
}
