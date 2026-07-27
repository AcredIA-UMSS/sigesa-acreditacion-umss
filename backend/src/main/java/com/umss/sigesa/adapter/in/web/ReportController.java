package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.GenerateExecutiveReportRequest;
import com.umss.sigesa.adapter.in.web.dto.ReportJobAcceptedResponse;
import com.umss.sigesa.adapter.in.web.dto.ReportJobStatusResponse;
import com.umss.sigesa.application.port.in.DownloadReportArtifactUseCase;
import com.umss.sigesa.application.port.in.GenerateExecutiveReportUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/executive/pdf")
public class ReportController {

    private final GenerateExecutiveReportUseCase generateExecutiveReportUseCase;
    private final GetReportJobStatusUseCase getReportJobStatusUseCase;
    private final DownloadReportArtifactUseCase downloadReportArtifactUseCase;

    public ReportController(GenerateExecutiveReportUseCase generateExecutiveReportUseCase,
                            GetReportJobStatusUseCase getReportJobStatusUseCase,
                            DownloadReportArtifactUseCase downloadReportArtifactUseCase) {
        this.generateExecutiveReportUseCase = generateExecutiveReportUseCase;
        this.getReportJobStatusUseCase = getReportJobStatusUseCase;
        this.downloadReportArtifactUseCase = downloadReportArtifactUseCase;
    }

    @PostMapping
    public ResponseEntity<ReportJobAcceptedResponse> generate(
            @Valid @RequestBody GenerateExecutiveReportRequest request,
            Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        ExecutiveReportFilters filters = new ExecutiveReportFilters(
                request.facultyId(),
                request.programId(),
                request.managementYear()
        );
        UUID jobId = generateExecutiveReportUseCase.generate(filters, requesterId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ReportJobAcceptedResponse(jobId));
    }

    @GetMapping("/{jobId}")
    public ReportJobStatusResponse getStatus(@PathVariable UUID jobId, Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        GetReportJobStatusUseCase.JobStatus status = getReportJobStatusUseCase.getStatus(jobId, requesterId);
        return new ReportJobStatusResponse(
                status.jobId(),
                status.status().name(),
                status.downloadPath(),
                status.errorCode()
        );
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID jobId, Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        DownloadReportArtifactUseCase.Artifact artifact =
                downloadReportArtifactUseCase.download(jobId, requesterId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + artifact.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(artifact.content());
    }
}
