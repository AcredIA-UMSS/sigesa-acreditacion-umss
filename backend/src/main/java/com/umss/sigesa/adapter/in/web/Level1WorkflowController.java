package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.Level1CompleteResponseDto;
import com.umss.sigesa.application.port.in.CloseLevel1UseCase;
import com.umss.sigesa.domain.model.Level1CompleteResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/level1-nodes/{level1Id}")
@Tag(name = "Level 1 workflow", description = "Cierre de Nivel 1 normativo (FSD-UC-010 / API-WF-03)")
public class Level1WorkflowController {

    private final CloseLevel1UseCase closeLevel1UseCase;

    public Level1WorkflowController(CloseLevel1UseCase closeLevel1UseCase) {
        this.closeLevel1UseCase = closeLevel1UseCase;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Cerrar Nivel 1 cuando todos los indicadores del subárbol están APROBADO")
    public ResponseEntity<Level1CompleteResponseDto> completeLevel1(
            @PathVariable UUID processId,
            @PathVariable UUID level1Id,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        Level1CompleteResult result = closeLevel1UseCase.close(
                processId,
                level1Id,
                actorId,
                primaryRole(authentication));
        return ResponseEntity.ok(toDto(result));
    }

    private static Level1CompleteResponseDto toDto(Level1CompleteResult result) {
        Level1CompleteResponseDto dto = new Level1CompleteResponseDto();
        dto.setLevel1Id(result.level1Id());
        dto.setPreviousState(result.previousState().name());
        dto.setNewState(result.newState().name());
        dto.setEvent(result.event());
        return dto;
    }

    private static String primaryRole(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .toList();
        return roles.isEmpty() ? "" : roles.getFirst();
    }
}
