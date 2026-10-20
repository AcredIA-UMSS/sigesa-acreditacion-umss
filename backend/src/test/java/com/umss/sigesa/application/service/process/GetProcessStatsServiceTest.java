package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessStats;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort.ProcessStatsRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProcessStatsServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;

    private GetProcessStatsService service;

    private final UUID careerA = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        service = new GetProcessStatsService(processQueryPort);
    }

    @Test
    void jdCountsStatusesModesAndFrozenCount() {
        when(processQueryPort.findAllStatsRows()).thenReturn(List.of(
                new ProcessStatsRow("ACTIVE", "ACTIVE"),
                new ProcessStatsRow("CLOSED", "AUDIT_READONLY")
        ));

        ProcessStats stats = service.getStats(new ProcessQueryContext("JD", List.of()));

        assertEquals(2, stats.total());
        assertEquals(1, stats.byStatus().get("ACTIVE"));
        assertEquals(1, stats.byStatus().get("CLOSED"));
        assertEquals(1, stats.byOperationalMode().get("ACTIVE"));
        assertEquals(1, stats.byOperationalMode().get("AUDIT_READONLY"));
        assertEquals(0, stats.byOperationalMode().get("IMPROVEMENT_MONITORING"));
        assertEquals(1, stats.frozenCount());
        assertEquals(stats.byOperationalMode().get("AUDIT_READONLY"), stats.frozenCount());
        assertEquals(stats.total(), sum(stats.byStatus()));
        assertEquals(stats.total(), sum(stats.byOperationalMode()));
        verify(processQueryPort).findAllStatsRows();
    }

    @Test
    void ccCountsOnlyAssignedCareers() {
        when(processQueryPort.findStatsRowsByCareerIds(List.of(careerA)))
                .thenReturn(List.of(new ProcessStatsRow("ACTIVE", "IMPROVEMENT_MONITORING")));

        ProcessStats stats = service.getStats(new ProcessQueryContext("CC", List.of(careerA)));

        assertEquals(1, stats.total());
        assertEquals(1, stats.byStatus().get("ACTIVE"));
        assertEquals(0, stats.byStatus().get("CLOSED"));
        assertEquals(1, stats.byOperationalMode().get("IMPROVEMENT_MONITORING"));
        assertEquals(0, stats.frozenCount());
        verify(processQueryPort).findStatsRowsByCareerIds(List.of(careerA));
        verify(processQueryPort, never()).findAllStatsRows();
    }

    @Test
    void emptyOrNullScopeReturnsZerosWithoutQueryingPort() {
        assertZeros(service.getStats(new ProcessQueryContext("CC", List.of())));
        assertZeros(service.getStats(new ProcessQueryContext("EE", null)));
        assertZeros(service.getStats(new ProcessQueryContext("CAE_MEMBER", List.of())));
        verifyNoInteractions(processQueryPort);
    }

    @Test
    void archivedAbsentFromFilteredPopulationIsNotReintroduced() {
        when(processQueryPort.findAllStatsRows()).thenReturn(List.of(
                new ProcessStatsRow("ACTIVE", "ACTIVE"),
                new ProcessStatsRow("CLOSED", "ACTIVE")
        ));

        ProcessStats stats = service.getStats(new ProcessQueryContext("TD", List.of()));

        assertFalse(stats.byStatus().containsKey("ARCHIVED"));
        assertEquals(Set.of("ACTIVE", "CLOSED"), stats.byStatus().keySet());
        assertEquals(2, stats.total());
        assertEquals(0, stats.frozenCount());
    }

    @Test
    void missingStatusAndModeStayAtZero() {
        when(processQueryPort.findAllStatsRows()).thenReturn(List.of(
                new ProcessStatsRow("ACTIVE", "ACTIVE")
        ));

        ProcessStats stats = service.getStats(new ProcessQueryContext("JD", List.of()));

        assertEquals(Set.of("ACTIVE", "CLOSED"), stats.byStatus().keySet());
        assertEquals(Set.of("ACTIVE", "AUDIT_READONLY", "IMPROVEMENT_MONITORING"), stats.byOperationalMode().keySet());
        assertEquals(1, stats.byStatus().get("ACTIVE"));
        assertEquals(0, stats.byStatus().get("CLOSED"));
        assertEquals(1, stats.byOperationalMode().get("ACTIVE"));
        assertEquals(0, stats.byOperationalMode().get("AUDIT_READONLY"));
        assertEquals(0, stats.byOperationalMode().get("IMPROVEMENT_MONITORING"));
        assertEquals(0, stats.frozenCount());
        assertEquals(stats.total(), sum(stats.byStatus()));
        assertEquals(stats.total(), sum(stats.byOperationalMode()));
    }

    private static void assertZeros(ProcessStats stats) {
        assertEquals(0, stats.total());
        assertEquals(0, stats.frozenCount());
        assertEquals(Set.of("ACTIVE", "CLOSED"), stats.byStatus().keySet());
        assertEquals(Set.of("ACTIVE", "AUDIT_READONLY", "IMPROVEMENT_MONITORING"), stats.byOperationalMode().keySet());
        assertEquals(0, sum(stats.byStatus()));
        assertEquals(0, sum(stats.byOperationalMode()));
    }

    private static int sum(Map<String, Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }
}
