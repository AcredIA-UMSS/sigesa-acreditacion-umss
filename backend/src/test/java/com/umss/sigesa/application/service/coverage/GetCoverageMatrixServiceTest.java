package com.umss.sigesa.application.service.coverage;

import com.umss.sigesa.application.port.out.CoverageMatrixPdfPort;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.CoverageIndicatorInput;
import com.umss.sigesa.domain.model.CoverageMatrix;
import com.umss.sigesa.domain.model.IndicatorState;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCoverageMatrixServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private CoverageMatrixQueryPort queryPort;
    @Mock
    private CoverageMatrixPdfPort pdfPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private GetCoverageMatrixService service;

    @BeforeEach
    void setUp() {
        service = new GetCoverageMatrixService(processQueryPort, queryPort, pdfPort, assignmentRepository);
    }

    @Test
    void get_assemblesMatrixForVisibleProcess() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID dim = UUID.randomUUID();
        UUID component = UUID.randomUUID();
        UUID criterion = UUID.randomUUID();
        UUID indicator = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(queryPort.findCareerName(careerId)).thenReturn(Optional.of("Ingeniería de Sistemas"));
        when(queryPort.findIndicatorRows(processId)).thenReturn(List.of(
                new CoverageIndicatorInput(
                        dim, "Dimensión", 1,
                        component, "Componente", 1,
                        criterion, "Criterio", 1,
                        indicator, "IND-1", "Plan de estudios", IndicatorState.APROBADO.name(), 1, 1)));

        CoverageMatrix matrix = service.get(processId, UUID.randomUUID(), List.of("JD"), List.of());

        assertThat(matrix.careerName()).isEqualTo("Ingeniería de Sistemas");
        assertThat(matrix.totals().indicators()).isEqualTo(1);
        assertThat(matrix.dimensions().getFirst().components().getFirst().criteria().getFirst().name())
                .isEqualTo("Criterio");
    }

    @Test
    void get_hidesOtherCareerFromCoordinator() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(ProcessNotFoundException.class, () ->
                service.get(processId, userId, List.of("CC"), List.of()));
    }

    @Test
    void get_unknownProcess() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.empty());
        assertThrows(ProcessNotFoundException.class, () ->
                service.get(processId, UUID.randomUUID(), List.of("JD"), List.of()));
    }

    @Test
    void renderPdf_delegatesToPort() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(queryPort.findCareerName(careerId)).thenReturn(Optional.of("Carrera"));
        when(queryPort.findIndicatorRows(processId)).thenReturn(List.of());
        when(pdfPort.render(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[] {0x25, 0x50, 0x44, 0x46});

        byte[] pdf = service.renderPdf(processId, UUID.randomUUID(), List.of("TD"), List.of());

        assertThat(pdf).startsWith((byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46);
        verify(pdfPort).render(org.mockito.ArgumentMatchers.any());
    }
}
