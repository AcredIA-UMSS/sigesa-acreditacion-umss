package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.port.in.*;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessControllerDownloadUnitTest — Unit Tests for Dossier Download")
class ProcessControllerDownloadUnitTest {

    @Mock
    private CreateProcessUseCase createProcessUseCase;
    @Mock
    private ListProcessesUseCase listProcessesUseCase;
    @Mock
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @Mock
    private DeleteProcessUseCase deleteProcessUseCase;
    @Mock
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;
    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private NormativeStructureWebMapper normativeStructureWebMapper;
    @Mock
    private ExportProcessDossierUseCase exportProcessDossierUseCase;
    @Mock
    private GetProcessDossierExportJobStatusUseCase getProcessDossierExportJobStatusUseCase;

    private ProcessController controller;
    private final UUID mockUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new ProcessController(
                createProcessUseCase,
                listProcessesUseCase,
                getProcessDetailUseCase,
                deleteProcessUseCase,
                userProgramAssignmentRepositoryPort,
                processQueryPort,
                normativeStructureWebMapper,
                exportProcessDossierUseCase,
                getProcessDossierExportJobStatusUseCase
        );

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUserId.toString(),
                "credentials",
                List.of(new SimpleGrantedAuthority("ROLE_JD"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("downloadDossier redirige con 302 FOUND cuando existe URL prefirmada S3")
    void downloadDossierRedirectsWhenPresignedUrlExists() {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String presignedUrl = "https://s3.amazonaws.com/sigesa-bucket/dossier.zip?signature=abc";

        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(UUID.randomUUID())
                .status("ACTIVE")
                .build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(getProcessDossierExportJobStatusUseCase.getPresignedDownloadUrl(jobId, mockUserId))
                .thenReturn(Optional.of(presignedUrl));

        ResponseEntity<?> response = controller.downloadDossier(processId, jobId);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(presignedUrl, response.getHeaders().getFirst(HttpHeaders.LOCATION));
    }

    @Test
    @DisplayName("downloadDossier retorna 200 OK con Content-Disposition resuelto por careerCode")
    void downloadDossierStreamsWithCareerCodeFilename() {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(UUID.randomUUID())
                .status("ACTIVE")
                .build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(getProcessDossierExportJobStatusUseCase.getPresignedDownloadUrl(jobId, mockUserId))
                .thenReturn(Optional.empty());

        ReportExportJob job = new ReportExportJob(jobId, mockUserId, processId, ReportFormat.ZIP, null,
                JobStatus.COMPLETED, 100, "dossier-exports/test.zip", null, Instant.now(), Instant.now());
        when(getProcessDossierExportJobStatusUseCase.getJobStatus(jobId, mockUserId)).thenReturn(job);

        InputStream mockStream = new ByteArrayInputStream("ZIP_BYTES".getBytes());
        when(getProcessDossierExportJobStatusUseCase.getJobFileStream(jobId, mockUserId)).thenReturn(mockStream);

        EnrichedProcessDetail detail = new EnrichedProcessDetail(
                processId,
                UUID.randomUUID(),
                "INF-SIS",
                "Ingeniería de Sistemas",
                UUID.randomUUID(),
                "Modelo CEUB 2026",
                "CEUB",
                "CEUB",
                "ACTIVE",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                null
        );
        when(getProcessDetailUseCase.getDetail(eq(processId), any())).thenReturn(detail);

        ResponseEntity<?> response = controller.downloadDossier(processId, jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=\"Dossier_INF-SIS.zip\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("downloadDossier utiliza careerName sanitizado cuando careerCode es nulo o vacío")
    void downloadDossierUsesSanitizedCareerNameWhenCodeEmpty() {
        UUID processId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(UUID.randomUUID())
                .status("ACTIVE")
                .build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        when(getProcessDossierExportJobStatusUseCase.getPresignedDownloadUrl(jobId, mockUserId))
                .thenReturn(Optional.empty());

        ReportExportJob job = new ReportExportJob(jobId, mockUserId, processId, ReportFormat.ZIP, null,
                JobStatus.COMPLETED, 100, "dossier-exports/test.zip", null, Instant.now(), Instant.now());
        when(getProcessDossierExportJobStatusUseCase.getJobStatus(jobId, mockUserId)).thenReturn(job);
        when(getProcessDossierExportJobStatusUseCase.getJobFileStream(jobId, mockUserId))
                .thenReturn(new ByteArrayInputStream("CONTENT".getBytes()));

        EnrichedProcessDetail detail = new EnrichedProcessDetail(
                processId,
                UUID.randomUUID(),
                "",
                "Ingeniería Eléctrica",
                UUID.randomUUID(),
                "Modelo CEUB 2026",
                "CEUB",
                "CEUB",
                "ACTIVE",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                null
        );
        when(getProcessDetailUseCase.getDetail(eq(processId), any())).thenReturn(detail);

        ResponseEntity<?> response = controller.downloadDossier(processId, jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=\"Dossier_Ingenieria_Electrica.zip\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }
}
