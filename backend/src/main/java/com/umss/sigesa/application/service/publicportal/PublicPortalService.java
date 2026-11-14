package com.umss.sigesa.application.service.publicportal;

import com.umss.sigesa.application.port.in.DownloadPublicCertificateUseCase;
import com.umss.sigesa.application.port.in.GetPublicProgramStatusUseCase;
import com.umss.sigesa.application.port.in.ListPublicProgramsUseCase;
import com.umss.sigesa.application.port.out.PublicCertificatePdfRendererPort;
import com.umss.sigesa.application.port.out.PublicProgramQueryPort;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;

import java.util.List;
import java.util.Locale;

public class PublicPortalService
        implements ListPublicProgramsUseCase, GetPublicProgramStatusUseCase, DownloadPublicCertificateUseCase {

    private final PublicProgramQueryPort queryPort;
    private final PublicCertificatePdfRendererPort pdfRenderer;

    public PublicPortalService(PublicProgramQueryPort queryPort, PublicCertificatePdfRendererPort pdfRenderer) {
        this.queryPort = queryPort;
        this.pdfRenderer = pdfRenderer;
    }

    @Override
    public List<PublicProgramSummary> listPublished() {
        return queryPort.findPublished().stream()
                .map(snapshot -> new PublicProgramSummary(snapshot.slug(), snapshot.programName()))
                .toList();
    }

    @Override
    public PublicProgramStatus getPublished(String slug) {
        PublicProgramSnapshot snapshot = requirePublished(slug);
        return new PublicProgramStatus(
                snapshot.slug(),
                snapshot.programName(),
                snapshot.accreditationStatus(),
                snapshot.validFrom(),
                snapshot.validUntil(),
                snapshot.certificatePublished()
        );
    }

    @Override
    public CertificateArtifact downloadPublished(String slug) {
        PublicProgramSnapshot snapshot = requirePublished(slug);
        if (!snapshot.certificatePublished()) {
            throw new PublicResourceNotFoundException();
        }
        byte[] pdf = pdfRenderer.render(snapshot);
        return new CertificateArtifact(
                pdf,
                "certificado-" + snapshot.slug() + ".pdf",
                snapshot.validFrom(),
                snapshot.validUntil()
        );
    }

    private PublicProgramSnapshot requirePublished(String slug) {
        String normalized = slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
        return queryPort.findBySlug(normalized)
                .filter(PublicProgramSnapshot::published)
                .orElseThrow(PublicResourceNotFoundException::new);
    }
}
