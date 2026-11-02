package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.application.port.in.NotificationModulePort;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;

import java.time.Duration;

public class InProcessNotificationModuleAdapter implements NotificationModulePort {

    private final NotificationOutboxJpaRepository outboxRepository;

    public InProcessNotificationModuleAdapter(NotificationOutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    public NotificationModuleSnapshot inspect(Duration deadline) {
        return new NotificationModuleSnapshot(
                outboxRepository.countByStatus("PENDING"),
                outboxRepository.count(),
                "in_process");
    }
}
