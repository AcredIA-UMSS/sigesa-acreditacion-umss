package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.umss.sigesa.config.PublicPortalSeedDataLoader;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfPublicCertificateRendererAdapterTest {

    private static final String BASE_URL = "http://localhost:5173";
    private final OpenPdfPublicCertificateRendererAdapter adapter =
            new OpenPdfPublicCertificateRendererAdapter(BASE_URL);

    @Test
    void renderIncludesRequiredFieldsMetadataAndSpanishGlyphs() throws Exception {
        PublicProgramSnapshot snapshot = snapshot(
                PublicPortalSeedDataLoader.SNAPSHOT_INF_SIS,
                "CEUB",
                "R-UMSS-12/2025"
        );

        byte[] pdf = adapter.render(snapshot);
        PdfReader reader = new PdfReader(pdf);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);

        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(reader.getNumberOfPages()).isEqualTo(1);
        assertThat(reader.getPageSize(1).getWidth()).isGreaterThan(reader.getPageSize(1).getHeight());
        assertThat(reader.getInfo().get("Title")).contains("Certificado de acreditación");
        assertThat(reader.getInfo().get("Title")).contains("Ingeniería de Sistemas");
        assertThat(reader.getInfo().get("Keywords")).contains("2025-01-01");
        assertThat(reader.getInfo().get("Keywords")).contains("2029-12-31");
        assertThat(text).contains("CERTIFICADO DE ACREDITACIÓN");
        assertThat(text).contains("Universidad Mayor de San Simón");
        assertThat(text).contains("Dirección Universitaria de Evaluación y Acreditación");
        assertThat(text).contains("Ingeniería de Sistemas");
        assertThat(text).contains("CEUB");
        assertThat(text).contains("Resolución N.º R-UMSS-12/2025");
        assertThat(text).contains("Firma y sello");
        assertThat(text).contains("N.º");
        assertThat(text).contains("Acreditación");
        assertThat(text).contains("Simón");
        assertThat(text).contains(PublicPortalSeedDataLoader.SNAPSHOT_INF_SIS.toString());
        reader.close();
    }

    @Test
    void renderKeepsLongNameWithEnyeOnOnePage() throws Exception {
        PublicProgramSnapshot snapshot = new PublicProgramSnapshot(
                UUID.fromString("660e8400-e29b-41d4-a716-446655440199"),
                "com-soc",
                "Licenciatura en Comunicación Social y Lengua Española — mención Ñuflo de Chávez",
                "ACREDITADA",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2029, 12, 31),
                true,
                true,
                "CEUB",
                "R-UMSS-01/2025"
        );

        byte[] pdf = adapter.render(snapshot);
        PdfReader reader = new PdfReader(pdf);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);

        assertThat(reader.getNumberOfPages()).isEqualTo(1);
        assertThat(text).contains("Ñuflo de Chávez");
        assertThat(text).contains("Licenciatura en Comunicación Social");
        reader.close();
    }

    @Test
    void renderOmitsOptionalFieldsAndWritesPublishedSample() throws Exception {
        PublicProgramSnapshot published = snapshot(PublicPortalSeedDataLoader.SNAPSHOT_INF_SIS, null, null);

        byte[] pdf = adapter.render(published);
        String text = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1);

        assertThat(text).contains("ha sido acreditada,");
        assertThat(text).doesNotContain(" por ");
        assertThat(text).doesNotContain("Resolución");

        Path sample = samplePath();
        Files.createDirectories(sample.getParent());
        Files.write(sample, pdf);
        assertThat(sample).exists();
        assertThat(Files.size(sample)).isGreaterThan(2000);
    }

    private static PublicProgramSnapshot snapshot(UUID id, String agency, String resolution) {
        return new PublicProgramSnapshot(
                id,
                PublicPortalSeedDataLoader.SLUG_INF_SIS,
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

    private static Path samplePath() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path moduleRoot = Files.exists(cwd.resolve("pom.xml")) && cwd.getFileName().toString().equals("backend")
                ? cwd
                : cwd.resolve("backend");
        return moduleRoot.resolve("target/certificate-sample-inf-sis.pdf");
    }
}
