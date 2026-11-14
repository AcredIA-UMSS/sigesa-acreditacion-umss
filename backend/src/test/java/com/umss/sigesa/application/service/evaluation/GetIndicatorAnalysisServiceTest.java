package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorAnalysisNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.IndicatorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetIndicatorAnalysisService — M8")
class GetIndicatorAnalysisServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private IndicatorAnalysisPort analysisPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private GetIndicatorAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new GetIndicatorAnalysisService(hierarchyQueryPort, analysisPort, assignmentRepository);
    }

    @Test
    void get_successForTd() {
        UUID indicatorId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, processId, UUID.randomUUID(), UUID.randomUUID(), "N1",
                        "IND-01", "Indicador", IndicatorState.PENDIENTE)));
        IndicatorAnalysis analysis = IndicatorAnalysis.builder()
                .id(UUID.randomUUID())
                .indicatorId(indicatorId)
                .processId(processId)
                .fodaQuadrant(FodaQuadrant.STRENGTH)
                .build();
        when(analysisPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(analysis));

        IndicatorAnalysis result = service.get(indicatorId, UUID.randomUUID(), List.of("TD"), List.of());
        assertEquals(FodaQuadrant.STRENGTH, result.getFodaQuadrant());
    }

    @Test
    void get_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());
        assertThrows(IndicatorNotFoundException.class,
                () -> service.get(indicatorId, UUID.randomUUID(), List.of("TD"), List.of()));
    }

    @Test
    void get_analysisNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "N1",
                        "IND-01", "Indicador", IndicatorState.PENDIENTE)));
        when(analysisPort.findByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorAnalysisNotFoundException.class,
                () -> service.get(indicatorId, UUID.randomUUID(), List.of("JD"), List.of()));
    }
}
