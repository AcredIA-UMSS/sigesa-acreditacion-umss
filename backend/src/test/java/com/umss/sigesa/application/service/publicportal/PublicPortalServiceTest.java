package com.umss.sigesa.application.service.publicportal;

import com.umss.sigesa.application.port.out.PublicCertificatePdfRendererPort;
import com.umss.sigesa.application.port.out.PublicProgramQueryPort;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicPortalServiceTest {

    @Mock
    private PublicProgramQueryPort queryPort;

    @Mock
    private PublicCertificatePdfRendererPort pdfRenderer;

    private PublicPortalService service;

    @BeforeEach
    void setUp() {
        service = new PublicPortalService(queryPort, pdfRenderer);
    }

    @Test
    void listPublishedExcludesDrafts() {
        when(queryPort.findPublished()).thenReturn(List.of(published("inf-sis", true)));

        var result = service.listPublished();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().slug()).isEqualTo("inf-sis");
        assertThat(result.getFirst().name()).isEqualTo("Ingeniería de Sistemas");
    }

    @Test
    void getPublishedThrowsGenericNotFoundForDraft() {
        when(queryPort.findBySlug("ing-civ")).thenReturn(Optional.of(unpublished()));

        assertThatThrownBy(() -> service.getPublished("ing-civ"))
                .isInstanceOf(PublicResourceNotFoundException.class)
                .hasMessage(PublicResourceNotFoundException.CLIENT_MESSAGE);
    }

    @Test
    void downloadThrowsWhenCertificateNotPublished() {
        when(queryPort.findBySlug("med")).thenReturn(Optional.of(published("med", false)));

        assertThatThrownBy(() -> service.downloadPublished("med"))
                .isInstanceOf(PublicResourceNotFoundException.class);
    }

    @Test
    void downloadReturnsPdfWithoutStoringUrl() {
        PublicProgramSnapshot snapshot = published("inf-sis", true);
        when(queryPort.findBySlug("inf-sis")).thenReturn(Optional.of(snapshot));
        when(pdfRenderer.render(snapshot)).thenReturn("%PDF-test".getBytes());

        var artifact = service.downloadPublished("INF-SIS");

        assertThat(artifact.filename()).isEqualTo("certificado-inf-sis.pdf");
        assertThat(artifact.content()).isEqualTo("%PDF-test".getBytes());
        assertThat(artifact.validFrom()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    private static PublicProgramSnapshot published(String slug, boolean certificate) {
        return new PublicProgramSnapshot(
                UUID.randomUUID(),
                slug,
                slug.equals("med") ? "Medicina" : "Ingeniería de Sistemas",
                "ACREDITADA",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2029, 12, 31),
                true,
                certificate,
                null,
                null
        );
    }

    private static PublicProgramSnapshot unpublished() {
        return new PublicProgramSnapshot(
                UUID.randomUUID(),
                "ing-civ",
                "Ingeniería Civil",
                "BORRADOR",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                false,
                false,
                null,
                null
        );
    }
}
