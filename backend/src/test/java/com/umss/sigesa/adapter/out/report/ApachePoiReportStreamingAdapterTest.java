package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApachePoiReportStreamingAdapter — SXSSF Streaming Test (R3)")
class ApachePoiReportStreamingAdapterTest {

    private final ApachePoiReportStreamingAdapter adapter = new ApachePoiReportStreamingAdapter();

    @Test
    @DisplayName("supports: Soporta XLSX y PDF")
    void supports_xlsxYPdf() {
        assertTrue(adapter.supports(ReportFormat.XLSX));
        assertTrue(adapter.supports(ReportFormat.PDF));
        assertFalse(adapter.supports(ReportFormat.CSV));
    }

    @Test
    @DisplayName("generateReport: Genera archivo Excel en streaming exitosamente")
    void generateReport_exitoso() {
        ObservationSummary obs = new ObservationSummary(
                "OBS-001", "IND-100", "3.1", "Laboratorio", "Incompleto",
                LocalDate.now(), LocalDate.now().plusDays(5), 5L, "PENDIENTE", "/link"
        );

        File file = adapter.generateReport(Stream.of(obs), ReportFormat.XLSX);

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
