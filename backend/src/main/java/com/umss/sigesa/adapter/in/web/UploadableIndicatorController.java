package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.UploadableIndicatorResponse;
import com.umss.sigesa.application.model.evidence.UploadableIndicator;
import com.umss.sigesa.application.port.in.ListUploadableIndicatorsUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/indicators")
@Tag(name = "Evidence", description = "Carga de evidencias (FSD-UC-004)")
public class UploadableIndicatorController {

    private final ListUploadableIndicatorsUseCase listUploadableIndicatorsUseCase;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UploadableIndicatorController(ListUploadableIndicatorsUseCase listUploadableIndicatorsUseCase,
                                         UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.listUploadableIndicatorsUseCase = listUploadableIndicatorsUseCase;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/uploadable")
    @PreAuthorize("hasRole('CC')")
    @Operation(
            summary = "Listar indicadores cargables",
            description = "Indicadores PENDIENTE/OBSERVADO de las carreras del [CC], con etiquetas para selects."
    )
    public ResponseEntity<List<UploadableIndicatorResponse>> listUploadable(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<UUID> programScope = assignmentRepository.findActiveByUserId(userId).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();
        // Tras reinicio H2 el userId del JWT puede no coincidir con el seed nuevo;
        // el claim programScope del token sigue siendo válido (IDs de carrera estables).
        if (programScope.isEmpty()) {
            programScope = programScopeFromJwtDetails(authentication);
        }
        List<UploadableIndicatorResponse> body = listUploadableIndicatorsUseCase.listForCoordinator(programScope)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    private static List<UUID> programScopeFromJwtDetails(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            return List.of();
        }
        Object details = authentication.getDetails();
        if (details instanceof List<?> list) {
            return list.stream()
                    .filter(UUID.class::isInstance)
                    .map(UUID.class::cast)
                    .toList();
        }
        return List.of();
    }

    private UploadableIndicatorResponse toResponse(UploadableIndicator item) {
        return new UploadableIndicatorResponse(
                item.indicatorId(),
                item.code(),
                item.title(),
                item.criterionId(),
                item.criterionCode(),
                item.criterionTitle(),
                item.currentState() == null ? null : item.currentState().name());
    }

    private static UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Usuario no autenticado.");
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }
}
