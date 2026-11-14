package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.InAppNotificationJpaEntity;
import com.umss.sigesa.application.port.out.InAppNotificationPort;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JpaInAppNotificationAdapter implements InAppNotificationPort {

    private final InAppNotificationJpaRepository repository;

    public JpaInAppNotificationAdapter(InAppNotificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(InAppNotification notification) {
        repository.save(toEntity(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InAppNotification> findByUserId(UUID userId, boolean unreadOnly, Pageable pageable) {
        Page<InAppNotificationJpaEntity> page = unreadOnly
                ? repository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::toDomain);
    }

    @Override
    @Transactional
    public boolean markRead(UUID notificationId, UUID userId) {
        return repository.findByIdAndUserId(notificationId, userId)
                .map(entity -> {
                    if (entity.getReadAt() == null) {
                        entity.setReadAt(LocalDateTime.now());
                        repository.save(entity);
                    }
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void markAllRead(UUID userId) {
        repository.markAllReadByUserId(userId, LocalDateTime.now());
    }

    private InAppNotificationJpaEntity toEntity(InAppNotification notification) {
        return InAppNotificationJpaEntity.builder()
                .id(notification.id())
                .outboxId(notification.outboxId())
                .userId(notification.userId())
                .eventType(notification.eventType())
                .title(notification.title())
                .body(notification.body())
                .deepLink(notification.deepLink())
                .programId(notification.programId())
                .processId(notification.processId())
                .readAt(notification.readAt())
                .createdAt(notification.createdAt())
                .build();
    }

    private InAppNotification toDomain(InAppNotificationJpaEntity entity) {
        return new InAppNotification(
                entity.getId(),
                entity.getOutboxId(),
                entity.getUserId(),
                entity.getEventType(),
                entity.getTitle(),
                entity.getBody(),
                entity.getDeepLink(),
                entity.getProgramId(),
                entity.getProcessId(),
                entity.getReadAt(),
                entity.getCreatedAt());
    }
}
