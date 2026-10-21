package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.model.notification.NotificationItem;
import com.umss.sigesa.application.port.out.NotificationQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationQueryJpaAdapter implements NotificationQueryPort {

    private final NotificationOutboxJpaRepository outboxRepository;

    public NotificationQueryJpaAdapter(NotificationOutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    public Page<NotificationItem> findNotificationsForUser(
            UUID userId,
            List<UUID> programIds,
            String email,
            boolean unreadOnly,
            Pageable pageable) {
        List<UUID> effectiveProgramIds = (programIds == null || programIds.isEmpty())
                ? List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                : programIds;

        String effectiveEmail = (email != null && !email.isBlank()) ? email : "none@umss.edu.bo";
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("00000000-0000-0000-0000-000000000000");

        return outboxRepository.findNotificationsForUser(effectiveUserId, effectiveProgramIds, effectiveEmail, unreadOnly, pageable)
                .map(this::toDomain);
    }

    @Override
    public long countUnread(UUID userId, List<UUID> programIds, String email) {
        List<UUID> effectiveProgramIds = (programIds == null || programIds.isEmpty())
                ? List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                : programIds;

        String effectiveEmail = (email != null && !email.isBlank()) ? email : "none@umss.edu.bo";
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("00000000-0000-0000-0000-000000000000");

        return outboxRepository.countUnreadForUser(effectiveUserId, effectiveProgramIds, effectiveEmail);
    }

    @Override
    public Optional<NotificationItem> findById(UUID notificationId) {
        return outboxRepository.findById(notificationId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        outboxRepository.findById(notificationId).ifPresent(entity -> {
            if (entity.getReadAt() == null) {
                entity.setReadAt(LocalDateTime.now());
                outboxRepository.save(entity);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId, List<UUID> programIds, String email) {
        List<UUID> effectiveProgramIds = (programIds == null || programIds.isEmpty())
                ? List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                : programIds;

        String effectiveEmail = (email != null && !email.isBlank()) ? email : "none@umss.edu.bo";
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("00000000-0000-0000-0000-000000000000");

        org.springframework.data.domain.Page<NotificationOutboxJpaEntity> unreadPage =
                outboxRepository.findNotificationsForUser(
                        effectiveUserId, effectiveProgramIds, effectiveEmail, true, Pageable.ofSize(100));

        LocalDateTime now = LocalDateTime.now();
        unreadPage.forEach(n -> n.setReadAt(now));
        outboxRepository.saveAll(unreadPage.getContent());
    }

    private NotificationItem toDomain(NotificationOutboxJpaEntity entity) {
        return new NotificationItem(
                entity.getId(),
                entity.getEventType(),
                entity.getRecipientId(),
                entity.getRecipientEmail(),
                entity.getSubject(),
                entity.getBody(),
                entity.getDeepLink(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }
}
