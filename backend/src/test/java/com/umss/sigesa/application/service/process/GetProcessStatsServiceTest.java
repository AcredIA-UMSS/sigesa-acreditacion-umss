package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessStats;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProcessStatsServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;

    private GetProcessStatsService service;

    private final UUID careerA = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID templateId = UUID.fromString("850e8400-e29b-41d4-a716-446655440010");

    @BeforeEach
    void setUp() {
        service = new GetProcessStatsService(processQueryPort);
    }

    @Test
    void jdAggregatesStatusAndOperationalMode() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(
                item("ACTIVE", "ACTIVE"),
                item("CLOSED", "AUDIT_READONLY"),
                item("ACTIVE", null)
        ));

        ProcessStats stats = service.stats(new ProcessQueryContext("JD", List.of()));

        assertEquals(3, stats.total());
        assertEquals(2L, stats.byStatus().get("ACTIVE"));
        assertEquals(1L, stats.byStatus().get("CLOSED"));
        assertEquals(2L, stats.byOperationalMode().get("ACTIVE"));
        assertEquals(1L, stats.byOperationalMode().get("AUDIT_READONLY"));
        assertEquals(1, stats.frozenCount());
        verify(processQueryPort).findAllSummaryItems();
    }

    @Test
    void ccWithEmptyScopeReturnsZerosWithoutQuery() {
        ProcessStats stats = service.stats(new ProcessQueryContext("CC", List.of()));

        assertEquals(0, stats.total());
        assertTrue(stats.byStatus().isEmpty());
        assertEquals(0, stats.frozenCount());
        verifyNoInteractions(processQueryPort);
    }

    @Test
    void ccUsesCareerFilter() {
        when(processQueryPort.findSummaryItemsByCareerIds(List.of(careerA)))
                .thenReturn(List.of(item("ACTIVE", "IMPROVEMENT_MONITORING")));

        ProcessStats stats = service.stats(new ProcessQueryContext("CC", List.of(careerA)));

        assertEquals(1, stats.total());
        assertEquals(1L, stats.byOperationalMode().get("IMPROVEMENT_MONITORING"));
        assertEquals(0, stats.frozenCount());
        verify(processQueryPort).findSummaryItemsByCareerIds(List.of(careerA));
    }

    private ProcessQueryPort.ProcessListItem item(String status, String operationalMode) {
        return new ProcessQueryPort.ProcessListItem(
                UUID.randomUUID(), careerA, templateId, status, LocalDateTime.now(), operationalMode);
    }
}
