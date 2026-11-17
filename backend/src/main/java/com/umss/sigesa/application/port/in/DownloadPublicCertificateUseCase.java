package com.umss.sigesa.application.port.in;

import java.time.LocalDate;

public interface DownloadPublicCertificateUseCase {

    record CertificateArtifact(
            byte[] content,
            String filename,
            LocalDate validFrom,
            LocalDate validUntil
    ) {}

    CertificateArtifact downloadPublished(String slug);
}
