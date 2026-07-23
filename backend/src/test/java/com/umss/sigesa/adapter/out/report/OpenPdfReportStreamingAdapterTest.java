package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenPdfReportStreamingAdapter — PDF Generation Test")
class OpenPdfReportStreamingAdapterTest {

    private final OpenPdfReportStreamingAdapter adapter = new OpenPdfReportStreamingAdapter();

    @Test
    @DisplayName("supports: Soporta PDF")
    void supports_pdfOnly() {
        assertTrue(adapter.supports(ReportFormat.PDF));
        assertFalse(adapter.supports(ReportFormat.XLSX));
        assertFalse(adapter.supports(ReportFormat.CSV));
    }

    @Test
    @DisplayName("generateReport: Genera archivo PDF exitosamente")
    void generateReport_exitoso() {
        ObservationSummary obs = new ObservationSummary(
                "OBS-001", "IND-100", "3.1", "Laboratorio", "Incompleto",
                LocalDate.now(), LocalDate.now().plusDays(5), 5L, "PENDIENTE", "/link"
        );

        File file = adapter.generateReport(Stream.of(obs), ReportFormat.PDF);

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
