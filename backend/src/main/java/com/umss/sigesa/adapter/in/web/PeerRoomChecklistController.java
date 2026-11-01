package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.PeerRoomChecklistItemDto;
import com.umss.sigesa.adapter.in.web.dto.UpdatePeerRoomChecklistRequestDto;
import com.umss.sigesa.application.port.in.GetPeerRoomChecklistUseCase;
import com.umss.sigesa.application.port.in.UpdatePeerRoomChecklistUseCase;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/processes/{processId}/peer-room-checklist")
@Tag(name = "Stage workflow", description = "Checklist Sala de Pares (API-WF-21 / FSD-UC-030)")
public class PeerRoomChecklistController {

    private final GetPeerRoomChecklistUseCase getPeerRoomChecklistUseCase;
    private final UpdatePeerRoomChecklistUseCase updatePeerRoomChecklistUseCase;

    public PeerRoomChecklistController(
            GetPeerRoomChecklistUseCase getPeerRoomChecklistUseCase,
            UpdatePeerRoomChecklistUseCase updatePeerRoomChecklistUseCase) {
        this.getPeerRoomChecklistUseCase = getPeerRoomChecklistUseCase;
        this.updatePeerRoomChecklistUseCase = updatePeerRoomChecklistUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CC','TD','JD','EE')")
    @Operation(operationId = "getPeerRoomChecklist", summary = "Consultar checklist Sala de Pares")
    public ResponseEntity<List<PeerRoomChecklistItemDto>> get(
            @PathVariable UUID processId,
            Authentication authentication) {
        List<PeerRoomChecklistItemDto> items = getPeerRoomChecklistUseCase.get(
                        processId,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(PeerRoomChecklistController::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('TD','EE')")
    @Operation(operationId = "updatePeerRoomChecklist", summary = "Actualizar ítems del checklist Sala de Pares")
    public ResponseEntity<List<PeerRoomChecklistItemDto>> update(
            @PathVariable UUID processId,
            @RequestBody UpdatePeerRoomChecklistRequestDto request,
            Authentication authentication) {
        List<UpdatePeerRoomChecklistUseCase.ChecklistUpdateItem> updates =
                request == null || request.getItems() == null
                        ? List.of()
                        : request.getItems().stream()
                                .map(item -> new UpdatePeerRoomChecklistUseCase.ChecklistUpdateItem(
                                        item.getItemCode(), item.isChecked()))
                                .toList();
        List<PeerRoomChecklistItemDto> items = updatePeerRoomChecklistUseCase.update(
                        processId,
                        updates,
                        (UUID) authentication.getPrincipal(),
                        extractRoles(authentication),
                        JwtProgramScopeSupport.programScopeFromAuthentication(authentication))
                .stream()
                .map(PeerRoomChecklistController::toDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    private static PeerRoomChecklistItemDto toDto(PeerRoomChecklistItem item) {
        return PeerRoomChecklistItemDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .label(item.getLabel())
                .checked(item.isChecked())
                .checkedBy(item.getCheckedBy())
                .checkedAt(item.getCheckedAt())
                .build();
    }

    private static List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();
    }
}
