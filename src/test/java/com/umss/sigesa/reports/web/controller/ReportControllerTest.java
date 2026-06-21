package com.umss.sigesa.reports.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

public class ReportControllerTest {

    @Test
    void export_directControllerInvocation_returnsRun() throws Exception {
        ReportService reportService = Mockito.mock(ReportService.class);
        ReportController controller = new ReportController(reportService);

        ReportRun run = ReportRun.builder().id(1L).status("PENDING").createdBy("actor").build();
        Mockito.when(reportService.submitExport(anyLong(), any(com.umss.sigesa.reports.dto.FilterPayload.class), anyString())).thenReturn(run);

        var response = controller.export(1L, new com.umss.sigesa.reports.dto.FilterPayload(), "actor");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }
}
