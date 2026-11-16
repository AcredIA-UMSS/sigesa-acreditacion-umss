package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.domain.model.CoverageMatrix;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfCoverageMatrixRendererAdapterTest {

    @Test
    void render_producesPdfBytesNotZip() {
        CoverageMatrix matrix = new CoverageMatrix(
                UUID.randomUUID(),
                "Ingeniería de Sistemas",
                Instant.parse("2026-11-15T15:00:00Z"),
                new CoverageMatrix.Totals(1, 1, 1, 0, 0, 100.0),
                List.of(new CoverageMatrix.DimensionNode(
                        UUID.randomUUID(),
                        "Contexto institucional",
                        List.of(new CoverageMatrix.ComponentNode(
                                UUID.randomUUID(),
                                "Misión",
                                List.of(new CoverageMatrix.CriterionNode(
                                        UUID.randomUUID(),
                                        "Criterio 1.1",
                                        List.of(new CoverageMatrix.IndicatorRow(
                                                UUID.randomUUID(),
                                                "IND-1.1.1",
                                                "Misión publicada",
                                                "APROBADO",
                                                2)))))))));

        byte[] pdf = new OpenPdfCoverageMatrixRendererAdapter().render(matrix);

        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(pdf).doesNotStartWith("PK".getBytes());
        assertThat(pdf.length).isGreaterThan(100);
    }
}
