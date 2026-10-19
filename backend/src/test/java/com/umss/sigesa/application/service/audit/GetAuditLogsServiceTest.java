package com.umss.sigesa.application.service.audit;

import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.model.AuditLogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GetAuditLogsServiceTest {

    @Test
    @DisplayName("getAuditLogs caps excessive page size to 100")
    void testGetAuditLogsCapsPageSize() {
        AuditLogQueryPort queryPort = mock(AuditLogQueryPort.class);
        GetAuditLogsService service = new GetAuditLogsService(queryPort);

        AuditLogEntry entry = AuditLogEntry.builder()
                .id(UUID.randomUUID())
                .actorEmail("test@umss.edu.bo")
                .action("LOGIN")
                .createdAt(LocalDateTime.now())
                .build();

        when(queryPort.findAuditLogs(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(entry)));

        Page<AuditLogEntry> result = service.getAuditLogs(
                "test@umss.edu.bo", "LOGIN", "USER", null, null, PageRequest.of(0, 500));

        assertThat(result.getContent()).hasSize(1);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryPort).findAuditLogs(eq("test@umss.edu.bo"), eq("LOGIN"), eq("USER"), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("streamCsvExport delegates to queryPort")
    void testStreamCsvExport() {
        AuditLogQueryPort queryPort = mock(AuditLogQueryPort.class);
        GetAuditLogsService service = new GetAuditLogsService(queryPort);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.streamCsvExport("actor@umss.bo", "ACT", "ENT", null, null, out);

        verify(queryPort).streamAuditLogsCsv(eq("actor@umss.bo"), eq("ACT"), eq("ENT"), any(), any(), eq(out));
    }

    @Test
    @DisplayName("getAuditLogs respects pageable within limit and handles unpaged requests")
    void testGetAuditLogsPreservesWithinLimit() {
        AuditLogQueryPort queryPort = mock(AuditLogQueryPort.class);
        GetAuditLogsService service = new GetAuditLogsService(queryPort);

        when(queryPort.findAuditLogs(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.getAuditLogs(null, null, null, null, null, PageRequest.of(1, 30));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryPort).findAuditLogs(eq(null), eq(null), eq(null), eq(null), eq(null), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(30);
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);

        // unpaged
        service.getAuditLogs(null, null, null, null, null, Pageable.unpaged());
        verify(queryPort).findAuditLogs(eq(null), eq(null), eq(null), eq(null), eq(null), eq(Pageable.unpaged()));
    }
}
