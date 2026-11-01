package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.CreateLevel1NodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.CreateNormativeIndicatorRequestDto;
import com.umss.sigesa.adapter.in.web.dto.CreateNormativeNodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorDetailResponseDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel1NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel2NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel3NodeDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateLevel1NodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateNormativeIndicatorRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateNormativeNodeRequestDto;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.in.GetNormativeIndicatorUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.NormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.UserProgramAssignment;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Jerarquía normativa v2", description = "Estructura N1–N3 + Indicador (release 2.0.0, API-STR)")
public class NormativeStructureController {

    private final GetProcessDetailUseCase getProcessDetailUseCase;
    private final GetNormativeIndicatorUseCase getNormativeIndicatorUseCase;
    private final NormativeStructureUseCases normativeStructureUseCases;
    private final NormativeStructurePort normativeStructurePort;
    private final NormativeStructureWebMapper normativeStructureWebMapper;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    @GetMapping("/processes/{processId}/level1-nodes")
    @PreAuthorize("hasAnyRole('JD','TD','CC')")
    @Operation(summary = "Listar nodos Nivel 1 del proceso")
    public ResponseEntity<List<NormativeLevel1NodeDto>> listLevel1Nodes(@PathVariable UUID processId) {
        var detail = getProcessDetailUseCase.getDetail(processId, buildQueryContext());
        return ResponseEntity.ok(normativeStructureWebMapper.toLevel1DtoList(
                detail.level1Nodes(), detail.evaluatorModel()));
    }

    @PostMapping("/processes/{processId}/level1-nodes")
    @PreAuthorize("hasAnyRole('JD','TD')")
    @Operation(summary = "Crear Nivel 1 en proceso ACTIVE")
    public ResponseEntity<NormativeLevel1NodeDto> addLevel1(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateLevel1NodeRequestDto request) {
        var detail = getProcessDetailUseCase.getDetail(processId, buildQueryContext());
        var node = normativeStructureUseCases.addLevel1(
                processId, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toLevel1Dto(node, detail.evaluatorModel()));
    }

    @PutMapping("/processes/{processId}/level1-nodes/{level1Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeLevel1NodeDto> updateLevel1(
            @PathVariable UUID processId,
            @PathVariable UUID level1Id,
            @RequestBody UpdateLevel1NodeRequestDto request) {
        var detail = getProcessDetailUseCase.getDetail(processId, buildQueryContext());
        var node = normativeStructureUseCases.updateLevel1(
                processId, level1Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toLevel1Dto(node, detail.evaluatorModel()));
    }

    @DeleteMapping("/processes/{processId}/level1-nodes/{level1Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<Void> deleteLevel1(@PathVariable UUID processId, @PathVariable UUID level1Id) {
        normativeStructureUseCases.deleteLevel1(processId, level1Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/level1-nodes/{level1Id}/level2-nodes")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeLevel2NodeDto> addLevel2(
            @PathVariable UUID level1Id,
            @Valid @RequestBody CreateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForProcess(normativeStructurePort.findProcessIdByLevel1(level1Id));
        var node = normativeStructureUseCases.addLevel2(
                level1Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toLevel2Dto(node, evaluatorModel));
    }

    @PutMapping("/level2-nodes/{level2Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeLevel2NodeDto> updateLevel2(
            @PathVariable UUID level2Id,
            @RequestBody UpdateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForProcess(normativeStructurePort.findProcessIdByLevel2(level2Id));
        var node = normativeStructureUseCases.updateLevel2(
                level2Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toLevel2Dto(node, evaluatorModel));
    }

    @DeleteMapping("/level2-nodes/{level2Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<Void> deleteLevel2(@PathVariable UUID level2Id) {
        normativeStructureUseCases.deleteLevel2(level2Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/level2-nodes/{level2Id}/level3-nodes")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeLevel3NodeDto> addLevel3(
            @PathVariable UUID level2Id,
            @Valid @RequestBody CreateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForProcess(normativeStructurePort.findProcessIdByLevel2(level2Id));
        var node = normativeStructureUseCases.addLevel3(
                level2Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toLevel3Dto(node, evaluatorModel));
    }

    @PutMapping("/level3-nodes/{level3Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeLevel3NodeDto> updateLevel3(
            @PathVariable UUID level3Id,
            @RequestBody UpdateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForProcess(normativeStructurePort.findProcessIdByLevel3(level3Id));
        var node = normativeStructureUseCases.updateLevel3(
                level3Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toLevel3Dto(node, evaluatorModel));
    }

    @DeleteMapping("/level3-nodes/{level3Id}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<Void> deleteLevel3(@PathVariable UUID level3Id) {
        normativeStructureUseCases.deleteLevel3(level3Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/level3-nodes/{level3Id}/indicators")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeIndicatorDto> addIndicator(
            @PathVariable UUID level3Id,
            @Valid @RequestBody CreateNormativeIndicatorRequestDto request) {
        var indicator = normativeStructureUseCases.addIndicator(
                level3Id,
                request.getCode(),
                request.getDescription(),
                request.getWeight(),
                request.getOrder(),
                request.getReferenceUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toIndicatorDto(indicator));
    }

    @PutMapping("/indicators/{indicatorId}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<NormativeIndicatorDto> updateIndicator(
            @PathVariable UUID indicatorId,
            @RequestBody UpdateNormativeIndicatorRequestDto request) {
        var indicator = normativeStructureUseCases.updateIndicator(
                indicatorId,
                request.getCode(),
                request.getDescription(),
                request.getWeight(),
                request.getOrder(),
                request.getReferenceUrl());
        return ResponseEntity.ok(normativeStructureWebMapper.toIndicatorDto(indicator));
    }

    @DeleteMapping("/indicators/{indicatorId}")
    @PreAuthorize("hasAnyRole('JD','TD')")
    public ResponseEntity<Void> deleteIndicator(@PathVariable UUID indicatorId) {
        normativeStructureUseCases.deleteIndicator(indicatorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/indicators/{indicatorId}")
    @PreAuthorize("hasAnyRole('JD','TD','CC')")
    @Operation(summary = "Detalle de indicador normativo")
    public ResponseEntity<NormativeIndicatorDetailResponseDto> getIndicator(@PathVariable UUID indicatorId) {
        var detail = getNormativeIndicatorUseCase.getById(indicatorId, buildQueryContext());
        return ResponseEntity.ok(normativeStructureWebMapper.toIndicatorDetailDto(detail));
    }

    private String evaluatorModelForProcess(UUID processId) {
        return getProcessDetailUseCase.getDetail(processId, buildQueryContext()).evaluatorModel();
    }

    private ProcessQueryContext buildQueryContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = extractRole(auth);
        UUID userId = extractUserId(auth);
        List<UUID> programScope = extractProgramScopes(userId);
        return new ProcessQueryContext(role, programScope);
    }

    private String extractRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return "";
        }
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (containsRole(authorities, "JD")) {
            return "JD";
        }
        if (containsRole(authorities, "TD")) {
            return "TD";
        }
        if (containsRole(authorities, "CC")) {
            return "CC";
        }
        return "";
    }

    private boolean containsRole(List<String> authorities, String role) {
        return authorities.contains("ROLE_" + role) || authorities.contains(role);
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
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private List<UUID> extractProgramScopes(UUID userId) {
        try {
            List<UserProgramAssignment> assignments = userProgramAssignmentRepositoryPort.findActiveByUserId(userId);
            return assignments.stream().map(UserProgramAssignment::getProgramId).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
