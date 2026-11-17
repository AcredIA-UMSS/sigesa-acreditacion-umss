package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.UpsertPeerVisitUseCase;
import com.umss.sigesa.application.port.out.PeerVisitPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidDateRangeException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.VisitImmutableException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PeerVisit;
import com.umss.sigesa.domain.model.PeerVisitItem;
import com.umss.sigesa.domain.model.PeerVisitItemType;
import com.umss.sigesa.domain.model.PeerVisitStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpsertPeerVisitServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private PeerVisitPort peerVisitPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private UpsertPeerVisitService service;

    @BeforeEach
    void setUp() {
        service = new UpsertPeerVisitService(processQueryPort, peerVisitPort, assignmentRepository);
    }

    @Test
    void upsert_persistsVenueRoomAndDocumentItem() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId);
        when(peerVisitPort.findByProcessId(processId)).thenReturn(Optional.empty());
        when(peerVisitPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PeerVisit saved = service.upsert(
                processId,
                command(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22), PeerVisitStatus.SCHEDULED),
                actorId,
                List.of("TD"),
                List.of());

        assertThat(saved.getVenue()).isEqualTo("Campus Central UMSS");
        assertThat(saved.getRoom()).isEqualTo("Sala de Pares 3");
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().getFirst().getType()).isEqualTo(PeerVisitItemType.DOCUMENT);
        ArgumentCaptor<PeerVisit> captor = ArgumentCaptor.forClass(PeerVisit.class);
        verify(peerVisitPort).save(captor.capture());
        assertThat(captor.getValue().getProcessId()).isEqualTo(processId);
    }

    @Test
    void upsert_rejectsInvertedDates() {
        UUID processId = UUID.randomUUID();
        stubProcess(processId);
        assertThrows(InvalidDateRangeException.class, () -> service.upsert(
                processId,
                command(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 20), PeerVisitStatus.SCHEDULED),
                UUID.randomUUID(),
                List.of("JD"),
                List.of()));
    }

    @Test
    void upsert_rejectsCompletedVisit() {
        UUID processId = UUID.randomUUID();
        stubProcess(processId);
        when(peerVisitPort.findByProcessId(processId)).thenReturn(Optional.of(PeerVisit.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .status(PeerVisitStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()));

        assertThrows(VisitImmutableException.class, () -> service.upsert(
                processId,
                command(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22), PeerVisitStatus.SCHEDULED),
                UUID.randomUUID(),
                List.of("TD"),
                List.of()));
    }

    @Test
    void upsert_rejectsCoordinatorWriter() {
        assertThrows(InvalidRoleException.class, () -> service.upsert(
                UUID.randomUUID(),
                command(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 22), PeerVisitStatus.SCHEDULED),
                UUID.randomUUID(),
                List.of("CC"),
                List.of()));
    }

    private void stubProcess(UUID processId) {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(UUID.randomUUID())
                        .status("ACTIVE")
                        .build()));
    }

    private static UpsertPeerVisitUseCase.Command command(LocalDate from, LocalDate to, PeerVisitStatus status) {
        return new UpsertPeerVisitUseCase.Command(
                from,
                to,
                "Campus Central UMSS",
                "Sala de Pares 3",
                status,
                "Semana ARCU-SUR",
                List.of(PeerVisitItem.builder()
                        .type(PeerVisitItemType.DOCUMENT)
                        .title("Informe de autoevaluación")
                        .build()));
    }
}
