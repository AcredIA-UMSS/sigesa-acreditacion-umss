package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;

public interface EnqueueDomainNotificationUseCase {

    /**
     * Resolves recipients and inserts EMAIL outbox + in-app rows in the current TX.
     * Never aborts the business transaction for recipient/email problems.
     */
    void enqueue(DomainNotificationCommand command);
}
