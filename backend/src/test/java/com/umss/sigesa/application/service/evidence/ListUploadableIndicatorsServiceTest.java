package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.evidence.UploadableIndicator;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListUploadableIndicatorsService — FSD-UC-004")
class ListUploadableIndicatorsServiceTest {

    @Mock
    private EvidenceControlQueryPort evidenceControlQueryPort;

    @InjectMocks
    private ListUploadableIndicatorsService service;

    @Test
    void emptyScope_returnsEmptyWithoutQuery() {
        assertTrue(service.listForCoordinator(List.of()).isEmpty());
        assertTrue(service.listForCoordinator(null).isEmpty());
        verifyNoInteractions(evidenceControlQueryPort);
    }

    @Test
    void withScope_queriesPendienteAndObservado() {
        UUID programId = UUID.randomUUID();
        UploadableIndicator item = new UploadableIndicator(
                UUID.randomUUID(),
                "IND-01",
                "Plan de estudios",
                UUID.randomUUID(),
                "CRIT-01",
                "Diseño curricular",
                IndicatorState.PENDIENTE);
        when(evidenceControlQueryPort.listUploadableByProgramIds(
                eq(List.of(programId)),
                eq(EnumSet.of(IndicatorState.PENDIENTE, IndicatorState.OBSERVADO))))
                .thenReturn(List.of(item));

        List<UploadableIndicator> result = service.listForCoordinator(List.of(programId));

        assertEquals(1, result.size());
        assertEquals("IND-01", result.getFirst().code());
        verify(evidenceControlQueryPort).listUploadableByProgramIds(
                eq(List.of(programId)),
                eq(EnumSet.of(IndicatorState.PENDIENTE, IndicatorState.OBSERVADO)));
    }
}
