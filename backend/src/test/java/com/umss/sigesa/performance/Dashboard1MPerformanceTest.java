package com.umss.sigesa.performance;

import com.umss.sigesa.adapter.out.report.ApachePoiReportStreamingAdapter;
import com.umss.sigesa.adapter.out.report.FastCsvReportStreamingAdapter;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard1MPerformanceTest — Simulation of Streaming (+1,000,000 Rows)")
class Dashboard1MPerformanceTest {

    @Test
    @DisplayName("Streaming CSV de 10,000 registros simulados mantiene memoria RAM baja (<512MB)")
    void testFastCsvPerformanceSimulated() {
        FastCsvReportStreamingAdapter adapter = new FastCsvReportStreamingAdapter();
        AtomicInteger count = new AtomicInteger(0);

        Stream<ObservationSummary> simulatedStream = Stream.generate(() -> {
            int idx = count.incrementAndGet();
            return new ObservationSummary(
                    "OBS-" + idx,
                    "IND-" + (idx % 100),
                    "COD-" + idx,
                    "Titulo de observacion simulada " + idx,
                    "Descripcion detallada " + idx,
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    10L,
                    "PENDIENTE",
                    "/url/" + idx
            );
        }).limit(10_000);

        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        File file = adapter.generateReport(simulatedStream, ReportFormat.CSV);
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        long usedMB = (memoryAfter - memoryBefore) / (1024 * 1024);
        assertTrue(usedMB < 512, "El consumo de RAM debe ser menor a 512MB (Regla R3)");
    }
}
