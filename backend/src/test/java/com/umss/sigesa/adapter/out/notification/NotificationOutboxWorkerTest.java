package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.out.NotificationSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationOutboxWorkerTest {

    private NotificationOutboxJpaRepository repo;
    private NotificationSenderPort sender;
    private NotificationOutboxWorker worker;

    @BeforeEach
    void setUp() {
        repo = mock(NotificationOutboxJpaRepository.class);
        sender = mock(NotificationSenderPort.class);
        PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        worker = new NotificationOutboxWorker(repo, sender, txManager);
    }

    @Test
    @DisplayName("Empty batch returns zero processed")
    void testProcessEmptyBatch() {
        when(repo.findPendingBatch(eq("PENDING"), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        int processed = worker.processPendingBatch(10);

        assertThat(processed).isZero();
        verifyNoInteractions(sender);
    }

    @Test
    @DisplayName("Claim moves PENDING to PROCESSING then SUCCESS marks SENT")
    void testClaimAndSuccess() {
        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = pendingEntity(notificationId);

        when(repo.findPendingBatch(eq("PENDING"), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(entity));
        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));

        int processed = worker.processPendingBatch(10);

        assertThat(processed).isEqualTo(1);
        verify(sender).send(notificationId, "test@umss.edu.bo", "Test subject", "Test body", "/link");
        // Same entity instance is mutated PENDING→PROCESSING→SENT; final persisted status must be SENT.
        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getSentAt()).isNotNull();
        verify(repo, atLeast(2)).save(entity);
    }

    @Test
    @DisplayName("Successful dispatch marks status SENT and sets sentAt")
    void testDispatchSuccess() {
        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = pendingEntity(notificationId);
        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));

        worker.dispatchNotification(notificationId);

        verify(sender).send(notificationId, "test@umss.edu.bo", "Test subject", "Test body", "/link");
        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("SENT");
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Failed dispatch increments retryCount, writes error_detail, backoff with jitter")
    void testDispatchFailureRetries() {
        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = pendingEntity(notificationId);
        entity.setRetryCount(1);
        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("SMTP connection timeout"))
                .when(sender).send(any(), any(), any(), any(), any());

        worker.dispatchNotification(notificationId);

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getRetryCount()).isEqualTo(2);
        assertThat(saved.getNextRetryAt()).isNotNull();
        assertThat(saved.getErrorDetail()).contains("SMTP");
    }

    @Test
    @DisplayName("Failed dispatch marks FAILED when maxRetries reached")
    void testDispatchMaxRetriesReached() {
        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = pendingEntity(notificationId);
        entity.setRetryCount(4);
        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("Fatal network failure"))
                .when(sender).send(any(), any(), any(), any(), any());

        worker.dispatchNotification(notificationId);

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(captor.getValue().getRetryCount()).isEqualTo(5);
        assertThat(captor.getValue().getErrorDetail()).isNotBlank();
    }

    @Test
    @DisplayName("dispatchNotification does nothing if notification does not exist or status is not PENDING/PROCESSING")
    void testDispatchNotificationNonPendingOrNotFound() {
        UUID notFoundId = UUID.randomUUID();
        when(repo.findById(notFoundId)).thenReturn(Optional.empty());
        worker.dispatchNotification(notFoundId);
        verifyNoInteractions(sender);

        UUID alreadySentId = UUID.randomUUID();
        NotificationOutboxJpaEntity sentEntity = NotificationOutboxJpaEntity.builder()
                .id(alreadySentId)
                .status("SENT")
                .build();
        when(repo.findById(alreadySentId)).thenReturn(Optional.of(sentEntity));
        worker.dispatchNotification(alreadySentId);
        verifyNoInteractions(sender);
    }

    private static NotificationOutboxJpaEntity pendingEntity(UUID notificationId) {
        return NotificationOutboxJpaEntity.builder()
                .id(notificationId)
                .eventType("IndicatorApproved")
                .recipientEmail("test@umss.edu.bo")
                .subject("Test subject")
                .body("Test body")
                .deepLink("/link")
                .channel("EMAIL")
                .idempotencyKey("IndicatorApproved:" + notificationId + ":user:EMAIL")
                .status("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
