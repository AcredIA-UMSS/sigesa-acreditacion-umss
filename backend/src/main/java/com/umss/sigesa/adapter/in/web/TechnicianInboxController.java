package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.TechnicianInboxResponseDto;
import com.umss.sigesa.application.port.in.GetTechnicianInboxUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboards/technician")
@Tag(name = "Technician Dashboard", description = "Bandeja centralizada de auditoría técnica y dictamen (FSD-UC-012)")
public class TechnicianInboxController {

    private final GetTechnicianInboxUseCase getTechnicianInboxUseCase;

    public TechnicianInboxController(GetTechnicianInboxUseCase getTechnicianInboxUseCase) {
        this.getTechnicianInboxUseCase = getTechnicianInboxUseCase;
    }

    @GetMapping("/inbox")
    @PreAuthorize("hasAnyRole('TD', 'JD')")
    @Operation(summary = "Obtener bandeja de evidencias pendientes de revisión técnica",
               description = "Lista paginada de indicadores pendientes con ruta normativa y metadatos.")
    public ResponseEntity<Page<TechnicianInboxResponseDto>> getInbox(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID level1Id,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TechnicianInboxResponseDto> result = getTechnicianInboxUseCase
                .getInbox(programId, level1Id, status, pageable)
                .map(TechnicianInboxResponseDto::from);

        return ResponseEntity.ok(result);
    }
}
