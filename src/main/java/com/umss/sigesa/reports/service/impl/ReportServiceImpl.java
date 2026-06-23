package com.umss.sigesa.reports.service.impl;

import com.umss.sigesa.reports.domain.ReportDefinition;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.repository.ReportDefinitionRepository;
import com.umss.sigesa.reports.repository.ReportRunRepository;
import com.umss.sigesa.reports.service.ReportService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportDefinitionRepository defRepo;
    private final ReportRunRepository runRepo;
    private final File exportDir;

    // ObjectMapper used to convert FilterPayload to a Map<String,Object> for persistence
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    public ReportServiceImpl(ReportDefinitionRepository defRepo, ReportRunRepository runRepo, @Value("${app.reports.export-dir:/tmp/sigesa-exports}") String exportDirPath) {
        this.defRepo = defRepo;
        this.runRepo = runRepo;
        this.exportDir = new File(exportDirPath);
        if (!this.exportDir.exists()) {
            this.exportDir.mkdirs();
        }
    }

    @Override
    @Transactional
    public ReportDefinition createDefinition(ReportDefinition def, String actor) {
        def.setCreatedAt(LocalDateTime.now());
        def.setUpdatedAt(LocalDateTime.now());
        return defRepo.save(def);
    }

    @Override
    @Transactional
    public ReportRun submitExport(Long definitionId, FilterPayload filter, String actor) {
        // persist the sanitized filter payload for audit in ReportRun.params
        java.util.Map<String, Object> paramsMap = new java.util.HashMap<>();
        if (filter != null) {
            try {
                paramsMap = MAPPER.convertValue(filter, java.util.Map.class);
            } catch (IllegalArgumentException ex) {
                // fallback to empty map if conversion fails
                paramsMap = new java.util.HashMap<>();
            }
        }

        ReportRun run = ReportRun.builder()
                .reportDefinitionId(definitionId)
                .params(paramsMap)
                .status("PENDING")
                .startedAt(LocalDateTime.now())
                .createdBy(actor)
                .build();
        run = runRepo.save(run);

        // spawn background worker thread
        final Long runId = run.getId();
        new Thread(() -> executeExport(runId, definitionId, filter, actor)).start();

        return run;
    }

    @Override
    public ReportRun getRun(Long runId, String actor) {
        Optional<ReportRun> r = runRepo.findById(runId);
        return r.orElse(null);
    }

    private void executeExport(Long runId, Long definitionId, FilterPayload filter, String actor) {
        Optional<ReportRun> maybe = runRepo.findById(runId);
        if (maybe.isEmpty()) return;
        ReportRun run = maybe.get();
        run.setStatus("PROCESSING");
        runRepo.save(run);

        String filename = "report_run_" + runId + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + ".xlsx";
        File out = new File(exportDir, filename);

        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            SXSSFSheet sheet1 = wb.createSheet("Resumen y Métricas");
            SXSSFSheet sheet2 = wb.createSheet("Datos Detallados");

            // sheet1: write simple metadata and a KPI row
            Row r0 = sheet1.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("Report Run ID");
            r0.createCell(1).setCellValue(runId);

            Row r1 = sheet1.createRow(2);
            r1.createCell(0).setCellValue("KPI");
            r1.createCell(1).setCellValue("Value");
            Row r2 = sheet1.createRow(3);
            r2.createCell(0).setCellValue("sample_kpi");
            r2.createCell(1).setCellValue(123);

            // sheet2: write header
            Row h = sheet2.createRow(0);
            h.createCell(0).setCellValue("careerName");
            h.createCell(1).setCellValue("facultyName");
            h.createCell(2).setCellValue("indicatorName");
            h.createCell(3).setCellValue("evidenceDescription");
            h.createCell(4).setCellValue("status");
            h.createCell(5).setCellValue("submissionDate");
            h.createCell(6).setCellValue("evidenceCount");

            // For demo: no real DB query — write zero rows or a sample row
            Row d1 = sheet2.createRow(1);
            d1.createCell(0).setCellValue("Sample Career");
            d1.createCell(1).setCellValue("Sample Faculty");
            d1.createCell(2).setCellValue("Sample Indicator");
            d1.createCell(3).setCellValue("Sample Evidence");
            d1.createCell(4).setCellValue("APROBADO");
            d1.createCell(5).setCellValue("2026-06-21");
            d1.createCell(6).setCellValue(1);

            try (FileOutputStream fos = new FileOutputStream(out)) {
                wb.write(fos);
            }

            // update run
            run.setStatus("COMPLETED");
            run.setFinishedAt(LocalDateTime.now());
            run.setDownloadUrl("file://" + out.getAbsolutePath());
            Map<String, Object> meta = new HashMap<>();
            meta.put("rows", 1);
            meta.put("file", out.getName());
            run.setResultMetadata(meta);
            runRepo.save(run);

        } catch (Exception ex) {
            run.setStatus("FAILED");
            Map<String, Object> err = new HashMap<>();
            err.put("error", ex.getClass().getSimpleName());
            err.put("message", ex.getMessage());
            run.setResultMetadata(err);
            run.setFinishedAt(LocalDateTime.now());
            runRepo.save(run);
        }

    }
}
