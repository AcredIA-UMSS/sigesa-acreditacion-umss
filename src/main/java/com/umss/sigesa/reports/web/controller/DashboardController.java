package com.umss.sigesa.reports.web.controller;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.dto.ReportDetailRowDTO;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final com.umss.sigesa.reports.security.SecurityInjector securityInjector;

    public DashboardController(DashboardService dashboardService, com.umss.sigesa.reports.security.SecurityInjector securityInjector) {
        this.dashboardService = dashboardService;
        this.securityInjector = securityInjector;
    }

    @GetMapping("/kpis")
    public ResponseEntity<List<ReportKpiDTO>> getKpis(FilterPayload filter, @RequestHeader(value = "X-Actor", required = false) String actor) {
        if (actor == null) actor = "unknown";
        FilterPayload safe = securityInjector.apply(filter, actor);
        Pageable p = PageRequest.of(0, 20);
        return ResponseEntity.ok(dashboardService.getKpis(safe, p, actor));
    }

    @GetMapping("/data")
    public ResponseEntity<Page<ReportDetailRowDTO>> getData(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, FilterPayload filter, @RequestHeader(value = "X-Actor", required = false) String actor) {
        if (actor == null) actor = "unknown";
        FilterPayload safe = securityInjector.apply(filter, actor);
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(dashboardService.getPaginatedData(safe, p, actor));
    }
}
