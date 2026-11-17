package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.CoverageMatrixResponseDto;
import com.umss.sigesa.application.port.in.GetCoverageMatrixUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/coverage-matrix")
@Tag(name = "Coverage matrix", description = "Informe de autoevaluación / matriz de cobertura (FSD-UC-039)")
public class CoverageMatrixController {

    private final GetCoverageMatrixUseCase getCoverageMatrixUseCase;

    public CoverageMatrixController(GetCoverageMatrixUseCase getCoverageMatrixUseCase) {
        this.getCoverageMatrixUseCase = getCoverageMatrixUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE','CAE_MEMBER')")
    @Operation(operationId = "getCoverageMatrix", summary = "Matriz criterio → indicador → evidencia → estado")
    public ResponseEntity<CoverageMatrixResponseDto> get(
            @PathVariable UUID processId,
            Authentication authentication) {
        return ResponseEntity.ok(CoverageMatrixResponseDto.from(getCoverageMatrixUseCase.get(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication))));
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE','CAE_MEMBER')")
    @Operation(operationId = "getCoverageMatrixPdf", summary = "PDF imprimible de la matriz de cobertura")
    public ResponseEntity<byte[]> pdf(
            @PathVariable UUID processId,
            Authentication authentication) {
        byte[] pdf = getCoverageMatrixUseCase.renderPdf(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"matriz-cobertura-" + processId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
