package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AuditLogResponseDto;
import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.domain.model.AuditLogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogControllerTest {

    @Test
    @DisplayName("getAuditLogs returns OK and paged DTOs")
    void testGetAuditLogs() {
        GetAuditLogsUseCase useCase = mock(GetAuditLogsUseCase.class);
        AuditLogController controller = new AuditLogController(useCase);

        UUID logId = UUID.randomUUID();
        AuditLogEntry entry = AuditLogEntry.builder()
                .id(logId)
                .actorEmail("jd@umss.edu.bo")
                .actorRole("JD")
                .action("STAGE_CLOSED")
                .entityType("STAGE")
                .entityId("STAGE-1")
                .createdAt(LocalDateTime.now())
                .build();

        when(useCase.getAuditLogs(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry)));

        ResponseEntity<Page<AuditLogResponseDto>> response = controller.getAuditLogs(
                "jd@umss.edu.bo", null, null, null, null, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(logId);
    }

    @Test
    @DisplayName("exportAuditLogs returns streaming response body with attachment header")
    void testExportAuditLogs() throws IOException {
        GetAuditLogsUseCase useCase = mock(GetAuditLogsUseCase.class);
        AuditLogController controller = new AuditLogController(useCase);

        doAnswer(invocation -> {
            ByteArrayOutputStream os = invocation.getArgument(5);
            os.write("id,action\n1,LOGIN".getBytes());
            return null;
        }).when(useCase).streamCsvExport(any(), any(), any(), any(), any(), any());

        ResponseEntity<StreamingResponseBody> response = controller.exportAuditLogs(
                null, null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .contains("audit_logs.csv");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getBody().writeTo(outputStream);
        assertThat(outputStream.toString()).contains("1,LOGIN");
    }

    @Test
    @DisplayName("getAuditLogs returns empty page when no logs match filters")
    void testGetAuditLogsEmpty() {
        GetAuditLogsUseCase useCase = mock(GetAuditLogsUseCase.class);
        AuditLogController controller = new AuditLogController(useCase);

        when(useCase.getAuditLogs(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<Page<AuditLogResponseDto>> response = controller.getAuditLogs(
                "none@umss.edu.bo", "UNKNOWN", "UNKNOWN", null, null, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isZero();
    }
}
