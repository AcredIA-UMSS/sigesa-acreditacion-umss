package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.PublicProgramStatusResponse;
import com.umss.sigesa.adapter.in.web.dto.PublicProgramSummaryResponse;
import com.umss.sigesa.application.port.in.DownloadPublicCertificateUseCase;
import com.umss.sigesa.application.port.in.GetPublicProgramStatusUseCase;
import com.umss.sigesa.application.port.in.ListPublicProgramsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/programs")
@Tag(name = "public-portal", description = "Portal público de acreditación (anónimo, FSD-UC-016)")
@SecurityRequirements
public class PublicProgramController {

    static final String HEADER_VALID_FROM = "X-Certificate-Valid-From";
    static final String HEADER_VALID_UNTIL = "X-Certificate-Valid-Until";
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ListPublicProgramsUseCase listPublicProgramsUseCase;
    private final GetPublicProgramStatusUseCase getPublicProgramStatusUseCase;
    private final DownloadPublicCertificateUseCase downloadPublicCertificateUseCase;

    public PublicProgramController(ListPublicProgramsUseCase listPublicProgramsUseCase,
                                   GetPublicProgramStatusUseCase getPublicProgramStatusUseCase,
                                   DownloadPublicCertificateUseCase downloadPublicCertificateUseCase) {
        this.listPublicProgramsUseCase = listPublicProgramsUseCase;
        this.getPublicProgramStatusUseCase = getPublicProgramStatusUseCase;
        this.downloadPublicCertificateUseCase = downloadPublicCertificateUseCase;
    }

    @GetMapping
    @Operation(
            operationId = "listPublicPrograms",
            summary = "Listar carreras publicadas",
            description = "Solo nombre y slug de programas con published=true. Filtro por nombre en el cliente.")
    public List<PublicProgramSummaryResponse> list() {
        return listPublicProgramsUseCase.listPublished().stream()
                .map(item -> new PublicProgramSummaryResponse(item.slug(), item.name()))
                .toList();
    }

    @GetMapping("/{slug}")
    @Operation(
            operationId = "getPublicProgram",
            summary = "Estado público de una carrera",
            description = "404 genérico si el slug no existe o no está publicado.")
    public PublicProgramStatusResponse get(@PathVariable String slug) {
        GetPublicProgramStatusUseCase.PublicProgramStatus status = getPublicProgramStatusUseCase.getPublished(slug);
        return new PublicProgramStatusResponse(
                status.slug(),
                status.name(),
                status.accreditationStatus(),
                status.validFrom(),
                status.validUntil(),
                status.certificateAvailable()
        );
    }

    @GetMapping(value = "/{slug}/certificate", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            operationId = "downloadPublicCertificate",
            summary = "Descargar certificado PDF",
            description = "PDF generado en memoria si el certificado está publicado. Sin URL de archivo. 404 genérico si no.")
    public ResponseEntity<byte[]> certificate(@PathVariable String slug) {
        DownloadPublicCertificateUseCase.CertificateArtifact artifact =
                downloadPublicCertificateUseCase.downloadPublished(slug);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + artifact.filename() + "\"")
                .header(HEADER_VALID_FROM, ISO_DATE.format(artifact.validFrom()))
                .header(HEADER_VALID_UNTIL, ISO_DATE.format(artifact.validUntil()))
                .body(artifact.content());
    }
}
