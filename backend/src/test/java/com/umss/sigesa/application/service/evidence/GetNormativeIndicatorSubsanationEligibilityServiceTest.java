package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetNormativeIndicatorSubsanationEligibilityService — FSD-UC-006 v2")
class GetNormativeIndicatorSubsanationEligibilityServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorObservationPort observationPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private GetNormativeIndicatorSubsanationEligibilityService service;

    @Test
    @DisplayName("CC en alcance con observación OPEN puede subsanar")
    void get_allowsCcWithOpenObservation() {
        UUID indicatorId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(observationId)
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));

        var result = service.get(indicatorId, userId, List.of("CC"));

        assertTrue(result.canSubsanate());
        assertEquals(observationId, result.openObservationId());
    }

    @Test
    @DisplayName("TD no puede subsanar")
    void get_rejectsNonCcRole() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));

        var result = service.get(indicatorId, UUID.randomUUID(), List.of("TD"));

        assertFalse(result.canSubsanate());
    }

    @Test
    @DisplayName("Indicador inexistente")
    void get_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () ->
                service.get(indicatorId, UUID.randomUUID(), List.of("CC")));
    }
}
