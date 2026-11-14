package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationChannel;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaNotificationOutboxAdapterTest {

    @Test
    @DisplayName("enqueueEvidenceUploaded delegates to dual-channel enqueue without synthetic emails")
    void testEnqueueEvidenceUploaded() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        EnqueueDomainNotificationUseCase enqueue = mock(EnqueueDomainNotificationUseCase.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo, enqueue);

        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        adapter.enqueueEvidenceUploaded(indicatorId, evidenceId, programId);

        ArgumentCaptor<DomainNotificationCommand> captor = ArgumentCaptor.forClass(DomainNotificationCommand.class);
        verify(enqueue).enqueue(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(NotificationEventType.EvidenceUploaded);
        assertThat(captor.getValue().aggregateId()).isEqualTo(indicatorId);
        assertThat(captor.getValue().programId()).isEqualTo(programId);
    }

    @Test
    @DisplayName("saveIfAbsent persists real recipient email and idempotency key")
    void testSaveIfAbsentUsesRealEmail() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        EnqueueDomainNotificationUseCase enqueue = mock(EnqueueDomainNotificationUseCase.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo, enqueue);

        when(repo.existsByIdempotencyKey(any())).thenReturn(false);

        UUID userId = UUID.randomUUID();
        adapter.saveIfAbsent(
                "IndicatorApproved:ind:user:EMAIL",
                NotificationEventType.IndicatorApproved,
                NotificationChannel.EMAIL,
                new NotificationRecipient(userId, "cc@umss.edu.bo", Role.CC),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "subject",
                "body",
                "/link",
                "{}",
                "PENDING");

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getRecipientEmail()).isEqualTo("cc@umss.edu.bo");
        assertThat(captor.getValue().getRecipientEmail()).doesNotContain("notificaciones-");
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("IndicatorApproved:ind:user:EMAIL");
        assertThat(captor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    @DisplayName("legacy enqueue delegates with programId from recipientId for IndicatorRejected")
    void testLegacyEnqueue() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        EnqueueDomainNotificationUseCase enqueue = mock(EnqueueDomainNotificationUseCase.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo, enqueue);

        UUID careerId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();

        adapter.enqueue("IndicatorRejected", careerId, Map.of(
                "indicatorId", indicatorId.toString(),
                "observationId", observationId.toString(),
                "newState", "OBSERVADO"
        ));

        ArgumentCaptor<DomainNotificationCommand> captor = ArgumentCaptor.forClass(DomainNotificationCommand.class);
        verify(enqueue).enqueue(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(NotificationEventType.IndicatorRejected);
        assertThat(captor.getValue().programId()).isEqualTo(careerId);
        assertThat(captor.getValue().payloadValue("observationId")).isEqualTo(observationId.toString());
    }
}
