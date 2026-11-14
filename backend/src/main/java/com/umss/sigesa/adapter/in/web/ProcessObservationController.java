package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.ObservationCommentRequest;
import com.umss.sigesa.adapter.in.web.dto.ObservationDraftRequest;
import com.umss.sigesa.adapter.in.web.dto.ObservationMessageResponse;
import com.umss.sigesa.adapter.in.web.dto.ObservationResponse;
import com.umss.sigesa.adapter.in.web.dto.ProcessEvidenceOptionResponse;
import com.umss.sigesa.application.model.observation.ObservationActor;
import com.umss.sigesa.application.model.observation.ObservationDraft;
import com.umss.sigesa.application.model.observation.ObservationFilter;
import com.umss.sigesa.application.port.in.ProcessObservationUseCase;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.observation.ObservationPriority;
import com.umss.sigesa.domain.model.observation.ObservationStatus;
import com.umss.sigesa.domain.model.observation.ProcessObservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/observations")
@RequiredArgsConstructor
@Tag(name = "Observaciones de proceso", description = "Retroalimentación del técnico y seguimiento del coordinador")
public class ProcessObservationController {

    private final ProcessObservationUseCase processObservationUseCase;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    @GetMapping("/evidences")
    @PreAuthorize("hasAnyRole('TD','CC')")
    @Operation(summary = "Evidencias del proceso que pueden recibir una observación")
    public ResponseEntity<List<ProcessEvidenceOptionResponse>> listEvidences(@PathVariable UUID processId) {
        List<ProcessEvidenceOptionResponse> body = processObservationUseCase.listEvidences(processId, actor()).stream()
                .map(this::toOption)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TD','CC')")
    @Operation(summary = "Listar observaciones del proceso")
    public ResponseEntity<List<ObservationResponse>> list(
            @PathVariable UUID processId,
            @RequestParam(required = false) ObservationStatus status,
            @RequestParam(required = false) ObservationPriority priority,
            @RequestParam(required = false) UUID evidenceId,
            @RequestParam(required = false) UUID authorId) {
        List<ObservationResponse> body = processObservationUseCase
                .list(processId, new ObservationFilter(status, priority, evidenceId, authorId), actor())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{observationId}")
    @PreAuthorize("hasAnyRole('TD','CC')")
    @Operation(summary = "Detalle de una observación y su historial")
    public ResponseEntity<ObservationResponse> get(
            @PathVariable UUID processId,
            @PathVariable UUID observationId) {
        return ResponseEntity.ok(toResponse(processObservationUseCase.get(processId, observationId, actor())));
    }

    @PostMapping
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Registrar una observación en borrador")
    public ResponseEntity<ObservationResponse> create(
            @PathVariable UUID processId,
            @Valid @RequestBody ObservationDraftRequest request) {
        ProcessObservation created = processObservationUseCase.create(processId, toDraft(request), actor());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{observationId}")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Actualizar un borrador")
    public ResponseEntity<ObservationResponse> update(
            @PathVariable UUID processId,
            @PathVariable UUID observationId,
            @Valid @RequestBody ObservationDraftRequest request) {
        return ResponseEntity.ok(toResponse(
                processObservationUseCase.update(processId, observationId, toDraft(request), actor())));
    }

    @PostMapping("/{observationId}/submit")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Enviar la retroalimentación al coordinador")
    public ResponseEntity<ObservationResponse> submit(
            @PathVariable UUID processId,
            @PathVariable UUID observationId) {
        return ResponseEntity.ok(toResponse(processObservationUseCase.submit(processId, observationId, actor())));
    }

    @PostMapping("/{observationId}/review")
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Marcar la observación en revisión")
    public ResponseEntity<ObservationResponse> review(
            @PathVariable UUID processId,
            @PathVariable UUID observationId) {
        return ResponseEntity.ok(toResponse(processObservationUseCase.startReview(processId, observationId, actor())));
    }

    @PostMapping("/{observationId}/reply")
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Responder la observación")
    public ResponseEntity<ObservationResponse> reply(
            @PathVariable UUID processId,
            @PathVariable UUID observationId,
            @Valid @RequestBody ObservationCommentRequest request) {
        return ResponseEntity.ok(toResponse(
                processObservationUseCase.reply(processId, observationId, request.body(), actor())));
    }

    @PostMapping("/{observationId}/attend")
    @PreAuthorize("hasRole('CC')")
    @Operation(summary = "Marcar la observación como atendida")
    public ResponseEntity<ObservationResponse> attend(
            @PathVariable UUID processId,
            @PathVariable UUID observationId,
            @Valid @RequestBody ObservationCommentRequest request) {
        return ResponseEntity.ok(toResponse(
                processObservationUseCase.attend(processId, observationId, request.body(), actor())));
    }

    @PostMapping("/{observationId}/close")
    @PreAuthorize("hasRole('TD')")
    @Operation(summary = "Cerrar la observación")
    public ResponseEntity<ObservationResponse> close(
            @PathVariable UUID processId,
            @PathVariable UUID observationId,
            @Valid @RequestBody ObservationCommentRequest request) {
        return ResponseEntity.ok(toResponse(
                processObservationUseCase.close(processId, observationId, request.body(), actor())));
    }

    private ObservationDraft toDraft(ObservationDraftRequest request) {
        return new ObservationDraft(
                request.evidenceId(),
                request.description(),
                request.kind(),
                request.priority(),
                request.compliance(),
                request.recommendation());
    }

    private ObservationResponse toResponse(ProcessObservation observation) {
        List<ObservationMessageResponse> messages = observation.getMessages().stream()
                .map(message -> new ObservationMessageResponse(
                        message.id(), message.authorId(), message.authorRole(), message.body(), message.createdAt()))
                .toList();
        return new ObservationResponse(
                observation.getId(),
                observation.getProcessId(),
                observation.getEvidenceId(),
                observation.getIndicatorId(),
                observation.getDescription(),
                observation.getKind().name(),
                observation.getPriority().name(),
                observation.getCompliance().name(),
                observation.getRecommendation(),
                observation.getAuthorId(),
                observation.getStatus().name(),
                observation.getCreatedAt(),
                observation.getUpdatedAt(),
                messages);
    }

    private ProcessEvidenceOptionResponse toOption(EvidenceLookupPort.EvidenceRef ref) {
        return new ProcessEvidenceOptionResponse(
                ref.evidenceId(),
                ref.indicatorId(),
                ref.code(),
                ref.title(),
                ref.originalFilename(),
                ref.documentAssetId(),
                ref.folioCode());
    }

    private ObservationActor actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = extractRole(auth);
        UUID userId = extractUserId(auth);
        List<UUID> scope = extractProgramScopes(userId);
        return new ObservationActor(userId, role, scope);
    }

    private String extractRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return "";
        }
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if (authorities.contains("ROLE_TD") || authorities.contains("TD")) {
            return "TD";
        }
        if (authorities.contains("ROLE_CC") || authorities.contains("CC")) {
            return "CC";
        }
        return "";
    }

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return UUID.randomUUID();
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (RuntimeException ex) {
            return UUID.randomUUID();
        }
    }

    private List<UUID> extractProgramScopes(UUID userId) {
        try {
            return userProgramAssignmentRepositoryPort.findActiveByUserId(userId).stream()
                    .map(UserProgramAssignment::getProgramId)
                    .toList();
        } catch (RuntimeException ex) {
            return List.of();
        }
    }
}
