package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectSubphaseIndicatorServiceTest {

    private static final String JUSTIFICATION = "La evidencia no cumple los requisitos minimos.";

    @Mock
    private SubphaseQueryPort subphaseQueryPort;
    @Mock
    private SubphaseEvidenceQueryPort evidenceQueryPort;
    @Mock
    private SubphaseObservationPort observationPort;
    @Mock
    private SubphaseTransitionHelper transitionHelper;
    @Mock
    private NotificationOutboxPort notificationOutbox;

    @InjectMocks
    private RejectSubphaseIndicatorService service;

    @Test
    void shouldRejectSubphaseAndCreateOpenObservation() {
        UUID subphaseId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        SubphaseObservation saved = SubphaseObservation.builder()
                .id(observationId)
                .subphaseId(subphaseId)
                .status(SubphaseObservationStatus.OPEN)
                .build();
        SubphaseTransitionResult transition = new SubphaseTransitionResult(
                subphaseId, SubphaseState.SUBIDO, SubphaseState.OBSERVADO);

        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, careerId, "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(true);
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.empty());
        when(observationPort.save(any())).thenReturn(saved);
        when(transitionHelper.transition(eq(subphaseId), eq(SubphaseState.OBSERVADO), any())).thenReturn(transition);

        var result = service.reject(subphaseId, JUSTIFICATION, actorId, "td");

        assertThat(result.subphaseId()).isEqualTo(subphaseId);
        assertThat(result.observationId()).isEqualTo(observationId);
        verify(notificationOutbox).enqueue(eq("SubphaseRejected"), eq(careerId), any());
    }

    @Test
    void shouldRejectWhenActorIsNotTechnician() {
        assertThatThrownBy(() -> service.reject(UUID.randomUUID(), JUSTIFICATION, UUID.randomUUID(), "CC"))
                .isInstanceOf(InvalidRoleException.class);
        verify(subphaseQueryPort, never()).findContext(any());
    }

    @Test
    void shouldThrowWhenJustificationIsTooShort() {
        assertThatThrownBy(() -> service.reject(UUID.randomUUID(), "muy corta", UUID.randomUUID(), "TD"))
                .isInstanceOf(JustificationRequiredException.class);
        verify(subphaseQueryPort, never()).findContext(any());
    }

    @Test
    void shouldThrowWhenSubphaseDoesNotExist() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(subphaseId, JUSTIFICATION, UUID.randomUUID(), "TD"))
                .isInstanceOf(ProcessNotFoundException.class);
        verify(observationPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenSubphaseHasNoEvidence() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(false);

        assertThatThrownBy(() -> service.reject(subphaseId, JUSTIFICATION, UUID.randomUUID(), "TD"))
                .isInstanceOf(EvidenceRequiredException.class);
        verify(observationPort, never()).save(any());
        verify(transitionHelper, never()).transition(any(), any(), any());
    }

    @Test
    void shouldThrowWhenOpenObservationAlreadyExists() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(true);
        when(observationPort.findLatestOpenBySubphaseId(subphaseId))
                .thenReturn(Optional.of(SubphaseObservation.builder()
                        .id(UUID.randomUUID())
                        .status(SubphaseObservationStatus.OPEN)
                        .createdAt(LocalDateTime.now())
                        .build()));

        assertThatThrownBy(() -> service.reject(subphaseId, JUSTIFICATION, UUID.randomUUID(), "TD"))
                .isInstanceOf(IllegalStateException.class);
        verify(observationPort, never()).save(any());
    }
}
