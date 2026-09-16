package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.SubphaseTransitionHelper;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.SubphaseEvidenceSubsanationCommand;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubsanateSubphaseEvidenceServiceTest {

    @Mock
    private SubphaseQueryPort subphaseQueryPort;
    @Mock
    private SubphaseObservationPort observationPort;
    @Mock
    private SubphaseEvidenceQueryPort evidenceQueryPort;
    @Mock
    private EvidenceUploadPersistencePort uploadPersistence;
    @Mock
    private EvidenceBlobStoragePort blobStorage;
    @Mock
    private ContentHashPort contentHashPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private SubphaseTransitionHelper transitionHelper;

    @InjectMocks
    private SubsanateSubphaseEvidenceService service;

    @Test
    void shouldSubsanateEvidenceAndTransitionToSubsanado() {
        UUID subphaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF".getBytes();
        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, evidenceId, observationId, "Nueva versión", pdf,
                "application/pdf", "fix.pdf", userId);

        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.of(
                SubphaseObservation.builder().id(observationId).build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, subphaseId)).thenReturn(Optional.of(
                new SubphaseEvidenceQueryPort.SubphaseEvidenceRef(
                        evidenceId, subphaseId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("hash");
        when(blobStorage.store(eq(evidenceId), eq(2), eq(pdf), eq("fix.pdf"))).thenReturn("new-key");
        when(uploadPersistence.persistSubphaseSubsanation(any(), any(), eq(observationId), eq(1), any()))
                .thenReturn("old-key");

        var result = service.subsanate(command);

        assertThat(result.version()).isEqualTo(2);
        assertThat(result.event()).isEqualTo(SubsanateSubphaseEvidenceService.EVENT_EVIDENCE_SUBSANATED);
        verify(blobStorage).delete("old-key");
        verify(transitionHelper).transition(eq(subphaseId), eq(SubphaseState.SUBSANADO), any());
    }

    @Test
    void shouldThrowWhenSubphaseDoesNotExist() {
        UUID subphaseId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.subsanate(command(subphaseId, UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(ProcessNotFoundException.class);
        verify(blobStorage, never()).store(any(), anyInt(), any(), any());
    }

    @Test
    void shouldThrowWhenUserHasNoProgramScope() {
        UUID subphaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, UUID.randomUUID(), "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, UUID.randomUUID(), UUID.randomUUID(), "desc", new byte[]{1},
                "application/pdf", "f.pdf", userId);

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(ProgramScopeDeniedException.class);
        verify(observationPort, never()).findLatestOpenBySubphaseId(any());
    }

    @Test
    void shouldThrowWhenNoOpenObservation() {
        UUID subphaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.empty());

        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, UUID.randomUUID(), UUID.randomUUID(), "desc", new byte[]{1},
                "application/pdf", "f.pdf", userId);

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(SubsanationNotAllowedException.class);
    }

    @Test
    void shouldThrowWhenObservationIdDoesNotMatchLatestOpen() {
        UUID subphaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.of(
                SubphaseObservation.builder().id(UUID.randomUUID()).build()));

        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, UUID.randomUUID(), UUID.randomUUID(), "desc", new byte[]{1},
                "application/pdf", "f.pdf", userId);

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(SubsanationNotAllowedException.class);
        verify(evidenceQueryPort, never()).findEvidenceRef(any(), any());
    }

    @Test
    void shouldThrowWhenEvidenceDoesNotBelongToSubphase() {
        UUID subphaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.of(
                SubphaseObservation.builder().id(observationId).build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, subphaseId)).thenReturn(Optional.empty());

        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, evidenceId, observationId, "desc", new byte[]{1},
                "application/pdf", "f.pdf", userId);

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(EvidenceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenDescriptionIsBlank() {
        UUID subphaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.of(
                SubphaseObservation.builder().id(observationId).build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, subphaseId)).thenReturn(Optional.of(
                new SubphaseEvidenceQueryPort.SubphaseEvidenceRef(
                        evidenceId, subphaseId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1)));

        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, evidenceId, observationId, "  ", new byte[]{1},
                "application/pdf", "f.pdf", userId);

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(EvidenceUnclassifiedException.class);
        verify(blobStorage, never()).store(any(), anyInt(), any(), any());
    }

    @Test
    void shouldDeleteBlobWhenPersistenceFails() {
        UUID subphaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF".getBytes();
        SubphaseEvidenceSubsanationCommand command = new SubphaseEvidenceSubsanationCommand(
                subphaseId, evidenceId, observationId, "Nueva versión", pdf,
                "application/pdf", "fix.pdf", userId);

        when(subphaseQueryPort.findContext(subphaseId))
                .thenReturn(Optional.of(new SubphaseQueryPort.SubphaseContext(subphaseId, programId, "S1")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenBySubphaseId(subphaseId)).thenReturn(Optional.of(
                SubphaseObservation.builder().id(observationId).build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, subphaseId)).thenReturn(Optional.of(
                new SubphaseEvidenceQueryPort.SubphaseEvidenceRef(
                        evidenceId, subphaseId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("hash");
        when(blobStorage.store(eq(evidenceId), eq(2), eq(pdf), eq("fix.pdf"))).thenReturn("new-key");
        when(uploadPersistence.persistSubphaseSubsanation(any(), any(), any(), anyInt(), any()))
                .thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.subsanate(command))
                .isInstanceOf(IllegalStateException.class);
        verify(blobStorage).delete("new-key");
        verify(transitionHelper, never()).transition(any(), any(), any());
    }

    private static SubphaseEvidenceSubsanationCommand command(UUID subphaseId, UUID evidenceId, UUID observationId) {
        return new SubphaseEvidenceSubsanationCommand(
                subphaseId, evidenceId, observationId, "desc", new byte[]{1},
                "application/pdf", "f.pdf", UUID.randomUUID());
    }
}
