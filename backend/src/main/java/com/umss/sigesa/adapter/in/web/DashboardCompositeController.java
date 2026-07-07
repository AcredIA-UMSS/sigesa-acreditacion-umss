package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ExportJobRequest;
import com.umss.sigesa.adapter.in.web.dto.ExportJobResponse;
import com.umss.sigesa.adapter.in.web.dto.JobStatusResponse;
import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetCompositeDashboardSummaryUseCase;
import com.umss.sigesa.application.port.in.GetCoordinatorObservationsDetailsUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.CompositeDashboardSummary;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardCompositeController {

    private final GetCompositeDashboardSummaryUseCase compositeSummaryUseCase;
    private final GetCoordinatorObservationsDetailsUseCase detailsUseCase;
    private final ExportReportJobUseCase exportReportJobUseCase;
    private final GetReportJobStatusUseCase getReportJobStatusUseCase;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    public DashboardCompositeController(GetCompositeDashboardSummaryUseCase compositeSummaryUseCase,
                                        GetCoordinatorObservationsDetailsUseCase detailsUseCase,
                                        ExportReportJobUseCase exportReportJobUseCase,
                                        GetReportJobStatusUseCase getReportJobStatusUseCase,
                                        UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort) {
        this.compositeSummaryUseCase = compositeSummaryUseCase;
        this.detailsUseCase = detailsUseCase;
        this.exportReportJobUseCase = exportReportJobUseCase;
        this.getReportJobStatusUseCase = getReportJobStatusUseCase;
        this.userProgramAssignmentRepositoryPort = userProgramAssignmentRepositoryPort;
    }

    @GetMapping("/me/summary")
    public ResponseEntity<CompositeDashboardSummary> getCompositeSummary() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<String> permissions = extractAuthorities(auth);
        List<UUID> programScopes = extractProgramScopes(userId);

        CompositeDashboardSummary summary = compositeSummaryUseCase.getSummaryForUser(userId, permissions, programScopes);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/coordinator/details")
    public ResponseEntity<Page<ObservationSummary>> getCoordinatorDetails(
            @RequestParam(required = false) Integer phaseId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<UUID> programScopes = extractProgramScopes(userId);
        UUID programId = programScopes.isEmpty() ? UUID.randomUUID() : programScopes.get(0);

        Page<ObservationSummary> details = detailsUseCase.getObservationsDetails(programId, phaseId, status, pageable);
        return ResponseEntity.ok(details);
    }

    @PostMapping({"/export-jobs", "/coordinator/export-jobs"})
    public ResponseEntity<ExportJobResponse> enqueueExportJob(@Valid @RequestBody ExportJobRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<UUID> programScopes = extractProgramScopes(userId);
        UUID programId = programScopes.isEmpty() ? UUID.randomUUID() : programScopes.get(0);

        ReportFormat format = ReportFormat.fromString(request.format());
        ReportExportJob job = exportReportJobUseCase.enqueueJob(userId, programId, format, request.phaseId());
        exportReportJobUseCase.processJobAsync(job.getJobId());

        String statusUrl = "/api/v1/dashboards/export-jobs/" + job.getJobId();
        ExportJobResponse response = new ExportJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                "Report generation has started in the background.",
                statusUrl
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/export-jobs/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable UUID jobId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);

        ReportExportJob job = getReportJobStatusUseCase.getJobStatus(jobId, userId);
        String downloadUrl = job.getStatus() == com.umss.sigesa.domain.model.JobStatus.COMPLETED
                ? "/api/v1/dashboards/export-jobs/" + job.getJobId() + "/download"
                : null;

        JobStatusResponse response = new JobStatusResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getProgressPercentage(),
                downloadUrl,
                job.getErrorMessage()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export-jobs/{jobId}/download")
    public ResponseEntity<InputStreamResource> downloadReport(@PathVariable UUID jobId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);

        ReportExportJob job = getReportJobStatusUseCase.getJobStatus(jobId, userId);
        InputStream fileStream = getReportJobStatusUseCase.getJobFileStream(jobId, userId);

        String ext = job.getFormat() == ReportFormat.CSV ? "csv" : (job.getFormat() == ReportFormat.PDF ? "pdf" : "xlsx");
        String filename = "dashboard_report_" + job.getJobId().toString().substring(0, 8) + "." + ext;

        MediaType mediaType = job.getFormat() == ReportFormat.CSV ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(new InputStreamResource(fileStream));
    }

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return UUID.randomUUID();
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private List<String> extractAuthorities(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return List.of();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private List<UUID> extractProgramScopes(UUID userId) {
        try {
            List<UserProgramAssignment> assignments = userProgramAssignmentRepositoryPort.findActiveByUserId(userId);
            return assignments.stream().map(UserProgramAssignment::getProgramId).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
