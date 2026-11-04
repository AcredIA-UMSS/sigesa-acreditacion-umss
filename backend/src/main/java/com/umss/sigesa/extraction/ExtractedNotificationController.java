package com.umss.sigesa.extraction;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExtractedNotificationController {

    private final NotificationOutboxJpaRepository outboxRepository;

    public ExtractedNotificationController(NotificationOutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @GetMapping("/internal/notifications/status")
    public ResponseEntity<NotificationModuleSnapshot> status() {
        return ResponseEntity.ok(new NotificationModuleSnapshot(
                outboxRepository.countByStatus("PENDING"),
                outboxRepository.count(),
                "extracted"));
    }

    @GetMapping("/api/health")
    public ResponseEntity<NotificationModuleSnapshot> health() {
        return status();
    }
}
