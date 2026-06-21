package com.umss.sigesa.reports.web.controller;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.service.ReportService;
import com.umss.sigesa.reports.security.SecurityInjector;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final SecurityInjector securityInjector;

    public ReportController(ReportService reportService, SecurityInjector securityInjector) {
        this.reportService = reportService;
        this.securityInjector = securityInjector;
    }

    @PostMapping("/{id}/export")
    public ResponseEntity<ReportRun> export(@PathVariable("id") Long id, @RequestBody @Valid FilterPayload filter, @RequestHeader(value = "X-Actor", required = false) String actor) {
        if (actor == null) actor = "unknown";
        // apply security constraints (may inject career scope for CC role)
        FilterPayload safe = securityInjector.apply(filter, actor);
        ReportRun run = reportService.submitExport(id, safe, actor);
        return ResponseEntity.accepted().body(run);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<ReportRun> getRun(@PathVariable("runId") Long runId, @RequestHeader(value = "X-Actor", required = false) String actor) {
        ReportRun run = reportService.getRun(runId, actor);
        if (run == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(run);
    }
}
