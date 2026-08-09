package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceMultipartRequest;
import com.umss.sigesa.adapter.in.web.dto.UploadEvidenceResponse;
import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.domain.model.EvidenceUploadCommand;
import com.umss.sigesa.domain.model.EvidenceUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}/evidences")
@Tag(name = "Evidence", description = "Carga de evidencias (FSD-UC-004)")
public class EvidenceController {

    private final UploadEvidenceUseCase uploadEvidenceUseCase;

    public EvidenceController(UploadEvidenceUseCase uploadEvidenceUseCase) {
        this.uploadEvidenceUseCase = uploadEvidenceUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cargar evidencia v1",
            description = "Multipart: file + criterionId + description. Rol CC. Respuesta 201."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Evidencia creada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UploadEvidenceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Metadatos incompletos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Fuera de alcance de carrera", content = @Content),
            @ApiResponse(responseCode = "404", description = "Indicador no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Indicador no cargable / upload en curso", content = @Content),
            @ApiResponse(responseCode = "413", description = "Archivo demasiado grande", content = @Content),
            @ApiResponse(responseCode = "422", description = "Formato inválido", content = @Content)
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = UploadEvidenceMultipartRequest.class)
            )
    )
    public ResponseEntity<UploadEvidenceResponse> upload(
            @PathVariable UUID indicatorId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("criterionId") UUID criterionId,
            @RequestPart("description") String description,
            Authentication authentication) throws IOException {

        UUID uploadedBy = (UUID) authentication.getPrincipal();
        EvidenceUploadCommand command = new EvidenceUploadCommand(
                indicatorId,
                criterionId,
                description,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename(),
                uploadedBy
        );

        EvidenceUploadResult result = uploadEvidenceUseCase.upload(command);
        UploadEvidenceResponse response = new UploadEvidenceResponse(
                result.evidenceId(),
                result.version(),
                result.contentHash(),
                result.event(),
                result.currentState().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
