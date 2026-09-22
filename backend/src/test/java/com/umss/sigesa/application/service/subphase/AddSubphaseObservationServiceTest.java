package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ApproveSubphaseIndicatorServiceTest {

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
    private ApproveSubphaseIndicatorService service;

    @Test
    void shouldApproveSubphaseWhenEvidenceExistsAndNoOpenObservation() {
        UUID subphaseId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        SubphaseTransitionResult transition = new SubphaseTransitionResult(
                subphaseId, SubphaseState.SUBIDO, SubphaseState.APROBADO);

        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, careerId, "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(true);
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.empty());
        when(transitionHelper.transition(eq(subphaseId), eq(SubphaseState.APROBADO), any())).thenReturn(transition);

        var result = service.approve(subphaseId, UUID.randomUUID(), "TD");

        assertThat(result.subphaseId()).isEqualTo(subphaseId);
        assertThat(result.transition().newState()).isEqualTo(SubphaseState.APROBADO);
        verify(notificationOutbox).enqueue(eq("SubphaseApproved"), eq(careerId), any());
    }

    @Test
    void shouldRejectWhenActorIsNotTechnician() {
        assertThatThrownBy(() -> service.approve(UUID.randomUUID(), UUID.randomUUID(), "JD"))
                .isInstanceOf(InvalidRoleException.class);
        verify(subphaseQueryPort, never()).findContext(any());
    }

    @Test
    void shouldThrowWhenSubphaseDoesNotExist() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(subphaseId, UUID.randomUUID(), "TD"))
                .isInstanceOf(ProcessNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSubphaseHasNoEvidence() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(false);

        assertThatThrownBy(() -> service.approve(subphaseId, UUID.randomUUID(), "TD"))
                .isInstanceOf(EvidenceRequiredException.class);
        verify(transitionHelper, never()).transition(any(), any(), any());
    }

    @Test
    void shouldThrowWhenOpenObservationBlocksApproval() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(evidenceQueryPort.hasEvidences(subphaseId)).thenReturn(true);
        when(observationPort.findLatestOpenBySubphaseId(subphaseId))
                .thenReturn(Optional.of(SubphaseObservation.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.approve(subphaseId, UUID.randomUUID(), "TD"))
                .isInstanceOf(SubsanationNotAllowedException.class);
        verify(transitionHelper, never()).transition(any(), any(), any());
    }
}
