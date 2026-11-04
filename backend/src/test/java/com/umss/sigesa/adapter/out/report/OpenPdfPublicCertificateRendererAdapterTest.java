package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfPublicCertificateRendererAdapterTest {

    @Test
    void renderIncludesValidityMetadata() {
        var snapshot = new PublicProgramSnapshot(
                UUID.randomUUID(),
                "inf-sis",
                "Ingeniería de Sistemas",
                "ACREDITADA",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2029, 12, 31),
                true,
                true
        );

        byte[] pdf = new OpenPdfPublicCertificateRendererAdapter().render(snapshot);

        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(pdf.length).isGreaterThan(200);
    }
}
