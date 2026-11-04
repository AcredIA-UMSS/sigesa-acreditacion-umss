package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessUseCase;
import com.umss.sigesa.application.port.in.ExportProcessDossierUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDossierExportJobStatusUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.domain.exception.ActiveExportAlreadyExistsException;
import com.umss.sigesa.domain.exception.TooManyConcurrentExportsException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        ProcessExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessDossierExportControllerTest {

    private static final UUID CC_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CAREER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;
    @MockitoBean
    private CreateProcessUseCase createProcessUseCase;
    @MockitoBean
    private ListProcessesUseCase listProcessesUseCase;
    @MockitoBean
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @MockitoBean
    private DeleteProcessUseCase deleteProcessUseCase;
    @MockitoBean
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;
    @MockitoBean
    private ProcessQueryPort processQueryPort;
    @MockitoBean
    private NormativeStructureWebMapper normativeStructureWebMapper;
    @MockitoBean
    private ExportProcessDossierUseCase exportProcessDossierUseCase;
    @MockitoBean
    private GetProcessDossierExportJobStatusUseCase getProcessDossierExportJobStatusUseCase;

    private static RequestPostProcessor ccAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                CC_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_CC"))));
    }

    @BeforeEach
    void setupAssignments() {
        UserProgramAssignment assignment = new UserProgramAssignment(
                UUID.randomUUID(), CC_USER_ID, CAREER_ID, LocalDateTime.now(), null);
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(CC_USER_ID))
                .thenReturn(List.of(assignment));
    }

    @Test
    @DisplayName("POST /api/v1/processes/{id}/dossier/export encola y devuelve 202")
    void enqueueDossierExportReturnsAccepted() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        ReportExportJob job = new ReportExportJob(jobId, CC_USER_ID, processId, ReportFormat.ZIP, null,
                JobStatus.PENDING, 0, null, null, Instant.now(), Instant.now());

        when(exportProcessDossierUseCase.enqueueJob(any(), any())).thenReturn(job);

        mockMvc.perform(post("/api/v1/processes/" + processId + "/dossier/export")
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(exportProcessDossierUseCase).processJobAsync(jobId);
    }

    @Test
    @DisplayName("POST /api/v1/processes/{id}/dossier/export devuelve 429 con Retry-After si el semáforo está saturado")
    void enqueueDossierExportReturns429WhenSaturated() throws Exception {
        UUID processId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(exportProcessDossierUseCase.enqueueJob(any(), any()))
                .thenThrow(new TooManyConcurrentExportsException("Capacity exceeded", 60));

        mockMvc.perform(post("/api/v1/processes/" + processId + "/dossier/export")
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }

    @Test
    @DisplayName("POST /api/v1/processes/{id}/dossier/export devuelve 409 si el usuario ya tiene un export activo")
    void enqueueDossierExportReturns409WhenUserHasActiveExport() throws Exception {
        UUID processId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(exportProcessDossierUseCase.enqueueJob(any(), any()))
                .thenThrow(new ActiveExportAlreadyExistsException("Active export already exists"));

        mockMvc.perform(post("/api/v1/processes/" + processId + "/dossier/export")
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ACTIVE_EXPORT_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/v1/processes/{id}/dossier/export devuelve 404 para CC intentando exportar otra carrera")
    void enqueueDossierExportRejectsDifferentCareerForCc() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID otherCareerId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(otherCareerId).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        mockMvc.perform(post("/api/v1/processes/" + processId + "/dossier/export")
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET status devuelve estado del job")
    void getStatusReturnsJobStatus() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        ReportExportJob job = new ReportExportJob(jobId, CC_USER_ID, processId, ReportFormat.ZIP, null,
                JobStatus.PROCESSING, 50, null, null, Instant.now(), Instant.now());

        when(getProcessDossierExportJobStatusUseCase.getJobStatus(jobId, CC_USER_ID)).thenReturn(job);

        mockMvc.perform(get("/api/v1/processes/" + processId + "/dossier/export-jobs/" + jobId)
                        .with(ccAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.progressPercentage").value(50));
    }

    @Test
    @DisplayName("GET download devuelve archivo directamente cuando no hay URL firmada")
    void downloadReturnsFileWhenCompleted() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        ReportExportJob job = new ReportExportJob(jobId, CC_USER_ID, processId, ReportFormat.ZIP, null,
                JobStatus.COMPLETED, 100, "dossier.zip", null, Instant.now(), Instant.now());

        when(getProcessDossierExportJobStatusUseCase.getPresignedDownloadUrl(jobId, CC_USER_ID))
                .thenReturn(Optional.empty());
        when(getProcessDossierExportJobStatusUseCase.getJobStatus(jobId, CC_USER_ID)).thenReturn(job);
        when(getProcessDossierExportJobStatusUseCase.getJobFileStream(jobId, CC_USER_ID))
                .thenReturn(new ByteArrayInputStream("ZIPDATA".getBytes()));

        mockMvc.perform(get("/api/v1/processes/" + processId + "/dossier/export-jobs/" + jobId + "/download")
                        .with(ccAuth()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET download redirige con 302 hacia URL firmada de S3 cuando está disponible")
    void downloadRedirectsToPresignedUrlWhenAvailable() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId).careerId(CAREER_ID).status("ACTIVE").build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        String presignedUrl = "https://s3.amazonaws.com/test-bucket/dossier-exports/test.zip?signature=xyz";
        when(getProcessDossierExportJobStatusUseCase.getPresignedDownloadUrl(jobId, CC_USER_ID))
                .thenReturn(Optional.of(presignedUrl));

        mockMvc.perform(get("/api/v1/processes/" + processId + "/dossier/export-jobs/" + jobId + "/download")
                        .with(ccAuth()))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
    }
}
