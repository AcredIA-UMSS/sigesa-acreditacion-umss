package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

@Component
public class ApachePoiReportStreamingAdapter implements ReportGeneratorPort {

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.XLSX || format == ReportFormat.PDF;
    }

    @Override
    public File generateReport(Stream<ObservationSummary> dataStream, ReportFormat format) {
        try {
            File tempFile = File.createTempFile("sigesa_report_", ".xlsx");
            tempFile.deleteOnExit();

            try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                
                Sheet sheet = workbook.createSheet("Observations");
                
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Observation ID", "Indicator", "Code", "Title", "Description", "Issue Date", "Due Date", "Status"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                int[] rowIdx = {1};
                dataStream.forEach(obs -> {
                    Row row = sheet.createRow(rowIdx[0]++);
                    row.createCell(0).setCellValue(obs.observationId() != null ? obs.observationId() : "");
                    row.createCell(1).setCellValue(obs.indicatorId() != null ? obs.indicatorId() : "");
                    row.createCell(2).setCellValue(obs.indicatorCode() != null ? obs.indicatorCode() : "");
                    row.createCell(3).setCellValue(obs.indicatorTitle() != null ? obs.indicatorTitle() : "");
                    row.createCell(4).setCellValue(obs.description() != null ? obs.description() : "");
                    row.createCell(5).setCellValue(obs.issueDate() != null ? obs.issueDate().toString() : "");
                    row.createCell(6).setCellValue(obs.dueDate() != null ? obs.dueDate().toString() : "");
                    row.createCell(7).setCellValue(obs.status() != null ? obs.status() : "");
                });

                workbook.write(out);
                workbook.dispose();
            }

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel streaming report", e);
        }
    }
}
