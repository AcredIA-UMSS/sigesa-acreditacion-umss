package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.out.NotificationSenderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

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

    @Test
    @DisplayName("Empty batch returns zero processed")
    void testProcessEmptyBatch() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        NotificationSenderPort sender = mock(NotificationSenderPort.class);

        when(repo.findPendingBatch(eq("PENDING"), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        NotificationOutboxWorker worker = new NotificationOutboxWorker(repo, sender);
        int processed = worker.processPendingBatch(10);

        assertThat(processed).isZero();
        verifyNoInteractions(sender);
    }

    @Test
    @DisplayName("Successful dispatch marks status SENT and sets sentAt")
    void testDispatchSuccess() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        NotificationSenderPort sender = mock(NotificationSenderPort.class);

        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(notificationId)
                .eventType("IndicatorApproved")
                .recipientEmail("test@umss.edu.bo")
                .subject("Test subject")
                .body("Test body")
                .deepLink("/link")
                .status("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));

        NotificationOutboxWorker worker = new NotificationOutboxWorker(repo, sender);
        worker.dispatchNotification(notificationId);

        verify(sender).send(notificationId, "test@umss.edu.bo", "Test subject", "Test body", "/link");

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("SENT");
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Failed dispatch increments retryCount and calculates backoff with jitter")
    void testDispatchFailureRetries() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        NotificationSenderPort sender = mock(NotificationSenderPort.class);

        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(notificationId)
                .eventType("IndicatorApproved")
                .recipientEmail("fail@umss.edu.bo")
                .subject("Fail subject")
                .body("Fail body")
                .status("PENDING")
                .retryCount(1)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("SMTP connection timeout"))
                .when(sender).send(any(), any(), any(), any(), any());

        NotificationOutboxWorker worker = new NotificationOutboxWorker(repo, sender);
        worker.dispatchNotification(notificationId);

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getRetryCount()).isEqualTo(2);
        assertThat(saved.getNextRetryAt()).isNotNull();
        assertThat(saved.getNextRetryAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Failed dispatch marks FAILED when maxRetries reached")
    void testDispatchMaxRetriesReached() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        NotificationSenderPort sender = mock(NotificationSenderPort.class);

        UUID notificationId = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(notificationId)
                .eventType("IndicatorApproved")
                .recipientEmail("fail@umss.edu.bo")
                .subject("Fail subject")
                .body("Fail body")
                .status("PENDING")
                .retryCount(4)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(repo.findById(notificationId)).thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("Fatal network failure"))
                .when(sender).send(any(), any(), any(), any(), any());

        NotificationOutboxWorker worker = new NotificationOutboxWorker(repo, sender);
        worker.dispatchNotification(notificationId);

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(saved.getRetryCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("dispatchNotification does nothing if notification does not exist or status is not PENDING")
    void testDispatchNotificationNonPendingOrNotFound() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        NotificationSenderPort sender = mock(NotificationSenderPort.class);

        UUID notFoundId = UUID.randomUUID();
        when(repo.findById(notFoundId)).thenReturn(Optional.empty());

        NotificationOutboxWorker worker = new NotificationOutboxWorker(repo, sender);
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
        verify(repo, never()).save(any());
    }
}
