package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JpaNotificationOutboxAdapterTest {

    @Test
    @DisplayName("enqueueEvidenceUploaded creates properly formatted outbox record")
    void testEnqueueEvidenceUploaded() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo);

        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        adapter.enqueueEvidenceUploaded(indicatorId, evidenceId, programId);

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventType()).isEqualTo("EvidenceUploaded");
        assertThat(saved.getRecipientId()).isEqualTo(programId);
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getDeepLink()).contains(programId.toString()).contains(indicatorId.toString());
        assertThat(saved.getSubject()).contains(indicatorId.toString());
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getMaxRetries()).isEqualTo(5);
    }

    @Test
    @DisplayName("enqueue for IndicatorRejected sets deep link with observationId and proper subject")
    void testEnqueueIndicatorRejected() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo);

        UUID recipientId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();

        adapter.enqueue("IndicatorRejected", recipientId, Map.of(
                "indicatorId", indicatorId.toString(),
                "observationId", observationId.toString(),
                "newState", "OBSERVADO"
        ));

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo("IndicatorRejected");
        assertThat(saved.getDeepLink()).contains("observationId=" + observationId);
        assertThat(saved.getSubject()).contains("Indicador observado");
        assertThat(saved.getBody()).contains("OBSERVADO");
    }

    @Test
    @DisplayName("enqueue for IndicatorApproved builds appropriate confirmation subject")
    void testEnqueueIndicatorApproved() {
        NotificationOutboxJpaRepository repo = mock(NotificationOutboxJpaRepository.class);
        JpaNotificationOutboxAdapter adapter = new JpaNotificationOutboxAdapter(repo);

        UUID recipientId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        adapter.enqueue("IndicatorApproved", recipientId, Map.of(
                "indicatorId", indicatorId.toString(),
                "newState", "APROBADO"
        ));

        ArgumentCaptor<NotificationOutboxJpaEntity> captor = ArgumentCaptor.forClass(NotificationOutboxJpaEntity.class);
        verify(repo).save(captor.capture());

        NotificationOutboxJpaEntity saved = captor.getValue();
        assertThat(saved.getSubject()).contains("Indicador aprobado");
    }
}
