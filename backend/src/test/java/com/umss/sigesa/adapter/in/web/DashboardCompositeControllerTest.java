package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.advice.DashboardExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetCompositeDashboardSummaryUseCase;
import com.umss.sigesa.application.port.in.GetCoordinatorObservationsDetailsUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.CompositeDashboardSummary;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardCompositeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DashboardExceptionHandler.class)
@DisplayName("DashboardCompositeController — Web MVC Test")
class DashboardCompositeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCompositeDashboardSummaryUseCase compositeSummaryUseCase;

    @MockitoBean
    private GetCoordinatorObservationsDetailsUseCase detailsUseCase;

    @MockitoBean
    private ExportReportJobUseCase exportReportJobUseCase;

    @MockitoBean
    private GetReportJobStatusUseCase getReportJobStatusUseCase;

    @MockitoBean
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @DisplayName("GET /api/v1/dashboards/me/summary: Returns 200 OK and composite summary")
    void getSummary_retornaOk() throws Exception {
        CompositeDashboardSummary summary = new CompositeDashboardSummary("usr-1", List.of(), null, null, null);
        when(compositeSummaryUseCase.getSummaryForUser(any(), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dashboards/me/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("usr-1"));
    }

    @Test
    @DisplayName("POST /api/v1/dashboards/export-jobs: Enqueues job and returns 202 Accepted")
    void enqueueExportJob_retorna202() throws Exception {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);
        when(exportReportJobUseCase.enqueueJob(any(), any(), any(), any())).thenReturn(job);

        mockMvc.perform(post("/api/v1/dashboards/export-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"format":"xlsx","phaseId":1}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Report generation has started in the background."));
    }
}
