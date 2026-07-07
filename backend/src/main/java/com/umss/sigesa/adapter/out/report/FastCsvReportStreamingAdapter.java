package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import de.siegmar.fastcsv.writer.CsvWriter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Component
public class FastCsvReportStreamingAdapter implements ReportGeneratorPort {

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.CSV;
    }

    @Override
    public File generateReport(Stream<ObservationSummary> dataStream, ReportFormat format) {
        try {
            File tempFile = File.createTempFile("sigesa_report_", ".csv");
            tempFile.deleteOnExit();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8));
                 CsvWriter csvWriter = CsvWriter.builder().build(bw)) {

                csvWriter.writeRecord("Observation ID", "Indicator", "Code", "Title", "Description", "Issue Date", "Due Date", "Status");

                dataStream.forEach(obs -> {
                    csvWriter.writeRecord(
                            obs.observationId() != null ? obs.observationId() : "",
                            obs.indicatorId() != null ? obs.indicatorId() : "",
                            obs.indicatorCode() != null ? obs.indicatorCode() : "",
                            obs.indicatorTitle() != null ? obs.indicatorTitle() : "",
                            obs.description() != null ? obs.description() : "",
                            obs.issueDate() != null ? obs.issueDate().toString() : "",
                            obs.dueDate() != null ? obs.dueDate().toString() : "",
                            obs.status() != null ? obs.status() : ""
                    );
                });
            }

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV streaming report", e);
        }
    }
}
