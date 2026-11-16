package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GetPeerVisitUseCase;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.application.port.out.PeerVisitPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPeerVisitServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private PeerVisitPort peerVisitPort;
    @Mock
    private CoverageMatrixQueryPort coverageMatrixQueryPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private GetPeerVisitService service;

    @BeforeEach
    void setUp() {
        service = new GetPeerVisitService(
                processQueryPort, peerVisitPort, coverageMatrixQueryPort, assignmentRepository);
    }

    @Test
    void get_returnsNullVisitWhenUnscheduled() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(coverageMatrixQueryPort.findCareerName(careerId)).thenReturn(Optional.of("Ingeniería de Sistemas"));
        when(peerVisitPort.findByProcessId(processId)).thenReturn(Optional.empty());

        GetPeerVisitUseCase.PeerVisitView view = service.get(processId, UUID.randomUUID(), List.of("EE"), List.of(careerId));

        assertThat(view.careerName()).isEqualTo("Ingeniería de Sistemas");
        assertThat(view.visit()).isEmpty();
    }

    @Test
    void get_unknownProcess() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.empty());
        assertThrows(ProcessNotFoundException.class, () ->
                service.get(processId, UUID.randomUUID(), List.of("JD"), List.of()));
    }
}
