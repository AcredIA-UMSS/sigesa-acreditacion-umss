package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorTransitionResult;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.SubphaseObservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
class RejectIndicatorServiceTest {

    private static final String JUSTIFICATION = "La evidencia no cumple los requisitos minimos.";

    @Mock
    private IndicatorRepositoryPort indicatorRepository;
    @Mock
    private SubphaseEvidenceQueryPort evidenceQueryPort;
    @Mock
    private SubphaseObservationPort observationPort;
    @Mock
    private IndicatorTransitionHelper transitionHelper;
    @Mock
    private NotificationOutboxPort notificationOutbox;

    @InjectMocks
    private RejectIndicatorService service;

    @Test
    void shouldRejectIndicatorWithoutLinkedSubphase() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, programId, UUID.randomUUID(), UUID.randomUUID());
        IndicatorTransitionResult transition = new IndicatorTransitionResult(
                indicatorId, IndicatorState.SUBIDO, IndicatorState.OBSERVADO, UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(evidenceQueryPort.hasEvidenceForIndicator(indicatorId)).thenReturn(true);
        when(evidenceQueryPort.findSubphaseIdsByIndicatorId(indicatorId)).thenReturn(List.of());
        when(transitionHelper.transition(eq(indicatorId), eq(IndicatorState.OBSERVADO), eq(actorId), eq(Role.TD), any()))
                .thenReturn(transition);

        var result = service.reject(indicatorId, JUSTIFICATION, actorId, "TD");

        assertThat(result.observationId()).isNull();
        assertThat(result.newState()).isEqualTo(IndicatorState.OBSERVADO);
        verify(observationPort, never()).save(any());
        verify(notificationOutbox).enqueue(eq("IndicatorRejected"), eq(programId), any());
    }

    @Test
    void shouldCreateObservationWhenIndicatorIsLinkedToSubphase() {
        UUID indicatorId = UUID.randomUUID();
        UUID subphaseId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(evidenceQueryPort.hasEvidenceForIndicator(indicatorId)).thenReturn(true);
        when(evidenceQueryPort.findSubphaseIdsByIndicatorId(indicatorId)).thenReturn(List.of(subphaseId));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.empty());
        when(observationPort.save(any())).thenReturn(SubphaseObservation.builder().id(observationId).build());
        when(transitionHelper.transition(any(), any(), any(), any(), any()))
                .thenReturn(new IndicatorTransitionResult(
                        indicatorId, IndicatorState.SUBIDO, IndicatorState.OBSERVADO, UUID.randomUUID()));

        var result = service.reject(indicatorId, JUSTIFICATION, actorId, "TD");

        assertThat(result.observationId()).isEqualTo(observationId);
        verify(observationPort).save(any());
    }

    @Test
    void shouldRejectWhenActorIsNotTechnician() {
        assertThatThrownBy(() -> service.reject(UUID.randomUUID(), JUSTIFICATION, UUID.randomUUID(), "CC"))
                .isInstanceOf(InvalidRoleException.class);
        verify(indicatorRepository, never()).findById(any());
    }

    @Test
    void shouldThrowWhenJustificationIsTooShort() {
        assertThatThrownBy(() -> service.reject(UUID.randomUUID(), "corta", UUID.randomUUID(), "TD"))
                .isInstanceOf(JustificationRequiredException.class);
    }

    @Test
    void shouldThrowWhenIndicatorDoesNotExist() {
        UUID indicatorId = UUID.randomUUID();
        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(indicatorId, JUSTIFICATION, UUID.randomUUID(), "TD"))
                .isInstanceOf(IndicatorNotFoundException.class);
        verify(transitionHelper, never()).transition(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowWhenIndicatorHasNoEvidence() {
        UUID indicatorId = UUID.randomUUID();
        when(indicatorRepository.findById(indicatorId))
                .thenReturn(Optional.of(new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), null)));
        when(evidenceQueryPort.hasEvidenceForIndicator(indicatorId)).thenReturn(false);

        assertThatThrownBy(() -> service.reject(indicatorId, JUSTIFICATION, UUID.randomUUID(), "TD"))
                .isInstanceOf(EvidenceRequiredException.class);
        verify(transitionHelper, never()).transition(any(), any(), any(), any(), any());
    }
}
