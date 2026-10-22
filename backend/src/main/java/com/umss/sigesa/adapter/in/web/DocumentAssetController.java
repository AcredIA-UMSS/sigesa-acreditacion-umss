package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.DocumentAssetResponseDto;
import com.umss.sigesa.application.port.in.GetDocumentAssetUseCase;
import com.umss.sigesa.domain.model.DocumentAssetView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/document-assets")
@Tag(name = "Document assets", description = "Consulta de documentos físicos (API-EVD-07)")
public class DocumentAssetController {

    private final GetDocumentAssetUseCase getDocumentAssetUseCase;

    public DocumentAssetController(GetDocumentAssetUseCase getDocumentAssetUseCase) {
        this.getDocumentAssetUseCase = getDocumentAssetUseCase;
    }

    @GetMapping("/{documentAssetId}")
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE','CAE_MEMBER')")
    @Operation(summary = "Obtener metadatos de un DocumentAsset")
    public ResponseEntity<DocumentAssetResponseDto> getDocumentAsset(
            @PathVariable UUID documentAssetId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        DocumentAssetView view = getDocumentAssetUseCase.get(documentAssetId, userId, extractRoles(authentication));
        return ResponseEntity.ok(toDto(view));
    }

    @GetMapping("/{documentAssetId}/content")
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE','CAE_MEMBER')")
    @Operation(summary = "Descargar blob del DocumentAsset (API-EVD-07b)")
    @ApiResponse(
            responseCode = "200",
            description = "Bytes del documento almacenado (MinIO/S3)",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(type = "string", format = "binary")))
    public ResponseEntity<byte[]> downloadContent(
            @PathVariable UUID documentAssetId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        GetDocumentAssetUseCase.DocumentAssetBinary binary =
                getDocumentAssetUseCase.download(documentAssetId, userId, extractRoles(authentication));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(binary.filename())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(binary.mediaType()))
                .body(binary.content());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD')")
    @Operation(summary = "Listar DocumentAsset de un proceso (selector de vínculo M7)")
    public ResponseEntity<List<DocumentAssetResponseDto>> listByProcess(
            @RequestParam UUID processId,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<DocumentAssetResponseDto> items = getDocumentAssetUseCase
                .listByProcess(processId, userId, extractRoles(authentication))
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    private DocumentAssetResponseDto toDto(DocumentAssetView view) {
        return DocumentAssetResponseDto.builder()
                .id(view.id())
                .folioCode(view.folioCode())
                .title(view.title())
                .contentHash(view.contentHash())
                .processId(view.processId())
                .linkedIndicatorIds(view.linkedIndicatorIds())
                .createdAt(view.createdAt())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
