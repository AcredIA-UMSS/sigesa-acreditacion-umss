package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddSubphaseObservationServiceTest {

    @Mock
    private SubphaseQueryPort subphaseQueryPort;
    @Mock
    private SubphaseObservationPort observationPort;

    @InjectMocks
    private AddSubphaseObservationService service;

    @Test
    void shouldAddOpenObservationForTechnician() {
        UUID subphaseId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.empty());
        when(observationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SubphaseObservation result = service.add(subphaseId, "  Falta anexo  ", authorId, "td");

        ArgumentCaptor<SubphaseObservation> captor = ArgumentCaptor.forClass(SubphaseObservation.class);
        verify(observationPort).save(captor.capture());
        assertThat(captor.getValue().getBody()).isEqualTo("Falta anexo");
        assertThat(captor.getValue().getAuthorRole()).isEqualTo("TD");
        assertThat(captor.getValue().getStatus()).isEqualTo(SubphaseObservationStatus.OPEN);
        assertThat(result.getSubphaseId()).isEqualTo(subphaseId);
    }

    @Test
    void shouldRejectWhenActorIsCoordinator() {
        assertThatThrownBy(() -> service.add(UUID.randomUUID(), "texto", UUID.randomUUID(), "CC"))
                .isInstanceOf(InvalidRoleException.class);
        verify(subphaseQueryPort, never()).findContext(any());
        verify(observationPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenBodyIsBlank() {
        assertThatThrownBy(() -> service.add(UUID.randomUUID(), "  ", UUID.randomUUID(), "JD"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(subphaseQueryPort, never()).findContext(any());
    }

    @Test
    void shouldThrowWhenSubphaseDoesNotExist() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(subphaseId, "Observación válida", UUID.randomUUID(), "JD"))
                .isInstanceOf(ProcessNotFoundException.class);
        verify(observationPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenOpenObservationAlreadyExists() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId))
                .thenReturn(Optional.of(SubphaseObservation.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.add(subphaseId, "Otra observación", UUID.randomUUID(), "TD"))
                .isInstanceOf(IllegalStateException.class);
        verify(observationPort, never()).save(any());
    }
}
