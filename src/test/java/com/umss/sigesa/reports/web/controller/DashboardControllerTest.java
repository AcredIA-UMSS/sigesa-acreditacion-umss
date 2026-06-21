package com.umss.sigesa.reports.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class DashboardControllerTest {

    @Test
    void getKpis_directControllerInvocation_returnsList() {
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        DashboardController controller = new DashboardController(dashboardService);

        ReportKpiDTO sample = new ReportKpiDTO("k1","Sample", BigDecimal.ONE);
        Mockito.when(dashboardService.getKpis(any(), any(), anyString())).thenReturn(List.of(sample));

        var response = controller.getKpis(new com.umss.sigesa.reports.dto.FilterPayload());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get(0).key()).isEqualTo("k1");
    }
}
