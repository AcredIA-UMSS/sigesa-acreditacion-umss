package com.umss.sigesa.application.service.audit;

import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.model.AuditAction;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import com.umss.sigesa.domain.model.AuditResourceType;
import com.umss.sigesa.domain.model.AuditResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAuditLogsService")
class GetAuditLogsServiceTest {

    @Mock
    private AuditLogQueryPort queryPort;

    @Test
    @DisplayName("acota el tamaño de página a 100 y ordena por fecha descendente")
    void capsPageSizeAndSortsByTime() {
        GetAuditLogsService service = new GetAuditLogsService(queryPort);
        when(queryPort.find(any(), any())).thenReturn(new PageImpl<>(List.of()));

        service.getAuditLogs(new AuditLogQuery(null, null, Set.of(), null, null, null, null, null),
                PageRequest.of(2, 500));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryPort).find(any(), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("occurredAt").getDirection());
    }

    @Test
    @DisplayName("rechaza un rango de fechas invertido")
    void rejectsInvertedRange() {
        GetAuditLogsService service = new GetAuditLogsService(queryPort);
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-01T00:00:00Z");

        assertThrows(InvalidFilterException.class, () -> service.getAuditLogs(
                new AuditLogQuery(null, null, Set.of(AuditAction.LOGIN), AuditResourceType.USER, null,
                        AuditResult.FAILURE, from, to),
                PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("devuelve el evento consultado por id")
    void returnsEventById() {
        GetAuditLogsService service = new GetAuditLogsService(queryPort);
        UUID id = UUID.randomUUID();
        AuditEvent event = new AuditEvent(id, null, null, AuditAction.LOGIN, AuditResourceType.USER,
                null, "Intento de inicio de sesión fallido", Instant.parse("2026-10-29T15:00:00Z"),
                AuditResult.FAILURE, null, null);
        when(queryPort.findById(id)).thenReturn(java.util.Optional.of(event));

        assertEquals(event, service.getAuditLog(id).orElseThrow());
    }
}
