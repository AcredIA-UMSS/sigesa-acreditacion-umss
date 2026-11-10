package com.umss.sigesa.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PublicCertificateTextTest {

    @Test
    void fullSentenceWhenAgencyAndResolutionPresent() {
        String sentence = PublicCertificateText.certificationSentence(snapshot("CEUB", "R-UMSS-12/2025"));

        assertThat(sentence).isEqualTo(
                "Se certifica que la carrera de Ingeniería de Sistemas ha sido acreditada por CEUB, "
                        + "con vigencia del 1 de enero de 2025 al 31 de diciembre de 2029, "
                        + "según Resolución N.º R-UMSS-12/2025.");
    }

    @Test
    void omitsAgencyAndResolutionWhenNull() {
        String sentence = PublicCertificateText.certificationSentence(snapshot(null, "  "));

        assertThat(sentence).isEqualTo(
                "Se certifica que la carrera de Ingeniería de Sistemas ha sido acreditada, "
                        + "con vigencia del 1 de enero de 2025 al 31 de diciembre de 2029.");
        assertThat(sentence).doesNotContain("por ");
        assertThat(sentence).doesNotContain("Resolución");
    }

    private static PublicProgramSnapshot snapshot(String agency, String resolution) {
        return new PublicProgramSnapshot(
                UUID.fromString("660e8400-e29b-41d4-a716-446655440100"),
                "inf-sis",
                "Ingeniería de Sistemas",
                "ACREDITADA",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2029, 12, 31),
                true,
                true,
                agency,
                resolution
        );
    }
}
