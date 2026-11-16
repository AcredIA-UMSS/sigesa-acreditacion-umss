package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.PeerVisitItemRequestDto;
import com.umss.sigesa.adapter.in.web.dto.PeerVisitResponseDto;
import com.umss.sigesa.adapter.in.web.dto.PeerVisitViewResponseDto;
import com.umss.sigesa.adapter.in.web.dto.UpsertPeerVisitRequestDto;
import com.umss.sigesa.application.port.in.GetPeerVisitUseCase;
import com.umss.sigesa.application.port.in.UpsertPeerVisitUseCase;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.PeerVisit;
import com.umss.sigesa.domain.model.PeerVisitItem;
import com.umss.sigesa.domain.model.PeerVisitItemType;
import com.umss.sigesa.domain.model.PeerVisitStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/peer-visit")
@Tag(name = "Peer visit", description = "Agenda de visita de pares (FSD-UC-040)")
public class PeerVisitController {

    private final GetPeerVisitUseCase getPeerVisitUseCase;
    private final UpsertPeerVisitUseCase upsertPeerVisitUseCase;

    public PeerVisitController(
            GetPeerVisitUseCase getPeerVisitUseCase,
            UpsertPeerVisitUseCase upsertPeerVisitUseCase) {
        this.getPeerVisitUseCase = getPeerVisitUseCase;
        this.upsertPeerVisitUseCase = upsertPeerVisitUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE')")
    @Operation(operationId = "getPeerVisit", summary = "Consultar agenda de visita de pares")
    public ResponseEntity<PeerVisitViewResponseDto> get(
            @PathVariable UUID processId,
            Authentication authentication) {
        GetPeerVisitUseCase.PeerVisitView view = getPeerVisitUseCase.get(
                processId,
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        PeerVisitResponseDto visit = view.visit().map(PeerVisitResponseDto::from).orElse(null);
        return ResponseEntity.ok(new PeerVisitViewResponseDto(view.processId(), view.careerName(), visit));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('TD','JD')")
    @Operation(operationId = "upsertPeerVisit", summary = "Crear o actualizar agenda de visita")
    public ResponseEntity<PeerVisitResponseDto> upsert(
            @PathVariable UUID processId,
            @RequestBody UpsertPeerVisitRequestDto request,
            Authentication authentication) {
        PeerVisit saved = upsertPeerVisitUseCase.upsert(
                processId,
                toCommand(request),
                (UUID) authentication.getPrincipal(),
                extractRoles(authentication),
                JwtProgramScopeSupport.programScopeFromAuthentication(authentication));
        return ResponseEntity.ok(PeerVisitResponseDto.from(saved));
    }

    private static UpsertPeerVisitUseCase.Command toCommand(UpsertPeerVisitRequestDto request) {
        UpsertPeerVisitRequestDto body = request != null ? request : new UpsertPeerVisitRequestDto();
        List<PeerVisitItem> items = new ArrayList<>();
        if (body.getItems() != null) {
            for (PeerVisitItemRequestDto item : body.getItems()) {
                if (item == null || item.getTitle() == null || item.getTitle().isBlank()) {
                    continue;
                }
                items.add(PeerVisitItem.builder()
                        .id(item.getId())
                        .type(parseType(item.getType()))
                        .title(item.getTitle().trim())
                        .scheduledAt(item.getScheduledAt())
                        .location(item.getLocation())
                        .notes(item.getNotes())
                        .done(item.isDone())
                        .build());
            }
        }
        return new UpsertPeerVisitUseCase.Command(
                body.getStartsOn(),
                body.getEndsOn(),
                body.getVenue(),
                body.getRoom(),
                parseStatus(body.getStatus()),
                body.getNotes(),
                items);
    }

    private static PeerVisitStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return PeerVisitStatus.SCHEDULED;
        }
        try {
            return PeerVisitStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Estado de visita inválido: " + raw);
        }
    }

    private static PeerVisitItemType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return PeerVisitItemType.DOCUMENT;
        }
        try {
            return PeerVisitItemType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Tipo de ítem de visita inválido: " + raw);
        }
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
