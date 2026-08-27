package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.DeprecatedEndpointResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoint legacy UC-004 por indicador — deprecado desde v1.1 (modelo subfase-centrado).
 * Use {@code POST /api/v1/subphases/{subphaseId}/evidences}.
 */
@RestController
@RequestMapping("/api/v1/indicators/{indicatorId}/evidences")
@Tag(name = "Evidence (legacy)", description = "Deprecado — usar carga por subfase (API-EVD-01)")
public class EvidenceController {

    @PostMapping
    @Operation(
            summary = "[Deprecado] Cargar evidencia por indicador",
            description = "Retorna 410 Gone. Sucesor: POST /api/v1/subphases/{subphaseId}/evidences (API-EVD-01).",
            deprecated = true
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "410",
                    description = "Endpoint retirado — migrar a carga por subfase",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeprecatedEndpointResponseDto.class)
                    )
            )
    })
    public ResponseEntity<DeprecatedEndpointResponseDto> upload(@PathVariable UUID indicatorId) {
        DeprecatedEndpointResponseDto body = new DeprecatedEndpointResponseDto();
        body.setError("ENDPOINT_DEPRECATED");
        body.setMessage(
                "La carga por indicador fue retirada. Use POST /api/v1/subphases/{subphaseId}/evidences.");
        body.setIndicatorId(indicatorId.toString());
        return ResponseEntity.status(HttpStatus.GONE)
                .header("Deprecation", "true")
                .header("Link", "</api/v1/subphases/{subphaseId}/evidences>; rel=\"successor-version\"")
                .body(body);
    }
}
