package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.CreateLevel1NodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.CreateNormativeIndicatorRequestDto;
import com.umss.sigesa.adapter.in.web.dto.CreateNormativeNodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel1NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel2NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel3NodeDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateLevel1NodeRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateNormativeIndicatorRequestDto;
import com.umss.sigesa.adapter.in.web.dto.UpdateNormativeNodeRequestDto;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.in.TemplateNormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.service.process.ProcessEnrichmentHelper;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JD')")
@Tag(name = "Jerarquía normativa plantilla v2", description = "Estructura N1–N3 + Indicador en plantilla DRAFT (API-TPL-08)")
public class TemplateNormativeStructureController {

    private final GetTemplateUseCase getTemplateUseCase;
    private final TemplateNormativeStructureUseCases templateNormativeStructureUseCases;
    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;
    private final NormativeStructureWebMapper normativeStructureWebMapper;

    @GetMapping("/templates/{templateId}/level1-nodes")
    @Operation(summary = "Listar nodos Nivel 1 de la plantilla")
    public ResponseEntity<List<NormativeLevel1NodeDto>> listLevel1Nodes(@PathVariable UUID templateId) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        List<TemplateLevel1Node> level1Nodes = loadSortedTemplateTree(templateId);
        return ResponseEntity.ok(normativeStructureWebMapper.toTemplateLevel1DtoList(level1Nodes, evaluatorModel));
    }

    @PostMapping("/templates/{templateId}/level1-nodes")
    @Operation(summary = "Crear Nivel 1 en plantilla DRAFT")
    public ResponseEntity<NormativeLevel1NodeDto> addLevel1(
            @PathVariable UUID templateId,
            @Valid @RequestBody CreateLevel1NodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.addLevel1(
                templateId, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toTemplateLevel1Dto(node, evaluatorModel));
    }

    @PutMapping("/templates/{templateId}/level1-nodes/{level1Id}")
    public ResponseEntity<NormativeLevel1NodeDto> updateLevel1(
            @PathVariable UUID templateId,
            @PathVariable UUID level1Id,
            @RequestBody UpdateLevel1NodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.updateLevel1(
                templateId, level1Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toTemplateLevel1Dto(node, evaluatorModel));
    }

    @DeleteMapping("/templates/{templateId}/level1-nodes/{level1Id}")
    public ResponseEntity<Void> deleteLevel1(@PathVariable UUID templateId, @PathVariable UUID level1Id) {
        templateNormativeStructureUseCases.deleteLevel1(templateId, level1Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/{templateId}/level1-nodes/{level1Id}/level2-nodes")
    public ResponseEntity<NormativeLevel2NodeDto> addLevel2(
            @PathVariable UUID templateId,
            @PathVariable UUID level1Id,
            @Valid @RequestBody CreateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.addLevel2(
                level1Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toTemplateLevel2Dto(node, evaluatorModel));
    }

    @PutMapping("/templates/{templateId}/level2-nodes/{level2Id}")
    public ResponseEntity<NormativeLevel2NodeDto> updateLevel2(
            @PathVariable UUID templateId,
            @PathVariable UUID level2Id,
            @RequestBody UpdateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.updateLevel2(
                level2Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toTemplateLevel2Dto(node, evaluatorModel));
    }

    @DeleteMapping("/templates/{templateId}/level2-nodes/{level2Id}")
    public ResponseEntity<Void> deleteLevel2(@PathVariable UUID templateId, @PathVariable UUID level2Id) {
        templateNormativeStructureUseCases.deleteLevel2(level2Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/{templateId}/level2-nodes/{level2Id}/level3-nodes")
    public ResponseEntity<NormativeLevel3NodeDto> addLevel3(
            @PathVariable UUID templateId,
            @PathVariable UUID level2Id,
            @Valid @RequestBody CreateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.addLevel3(
                level2Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toTemplateLevel3Dto(node, evaluatorModel));
    }

    @PutMapping("/templates/{templateId}/level3-nodes/{level3Id}")
    public ResponseEntity<NormativeLevel3NodeDto> updateLevel3(
            @PathVariable UUID templateId,
            @PathVariable UUID level3Id,
            @RequestBody UpdateNormativeNodeRequestDto request) {
        String evaluatorModel = evaluatorModelForTemplate(templateId);
        var node = templateNormativeStructureUseCases.updateLevel3(
                level3Id, request.getName(), request.getOrder(), request.getDescription());
        return ResponseEntity.ok(normativeStructureWebMapper.toTemplateLevel3Dto(node, evaluatorModel));
    }

    @DeleteMapping("/templates/{templateId}/level3-nodes/{level3Id}")
    public ResponseEntity<Void> deleteLevel3(@PathVariable UUID templateId, @PathVariable UUID level3Id) {
        templateNormativeStructureUseCases.deleteLevel3(level3Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/{templateId}/level3-nodes/{level3Id}/indicators")
    public ResponseEntity<NormativeIndicatorDto> addIndicator(
            @PathVariable UUID templateId,
            @PathVariable UUID level3Id,
            @Valid @RequestBody CreateNormativeIndicatorRequestDto request) {
        var indicator = templateNormativeStructureUseCases.addIndicator(
                level3Id,
                request.getCode(),
                request.getDescription(),
                request.getWeight(),
                request.getOrder(),
                request.getReferenceUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(normativeStructureWebMapper.toTemplateIndicatorDto(indicator));
    }

    @PutMapping("/template-indicators/{indicatorId}")
    public ResponseEntity<NormativeIndicatorDto> updateIndicator(
            @PathVariable UUID indicatorId,
            @RequestBody UpdateNormativeIndicatorRequestDto request) {
        var indicator = templateNormativeStructureUseCases.updateIndicator(
                indicatorId,
                request.getCode(),
                request.getDescription(),
                request.getWeight(),
                request.getOrder(),
                request.getReferenceUrl());
        return ResponseEntity.ok(normativeStructureWebMapper.toTemplateIndicatorDto(indicator));
    }

    @DeleteMapping("/template-indicators/{indicatorId}")
    public ResponseEntity<Void> deleteIndicator(@PathVariable UUID indicatorId) {
        templateNormativeStructureUseCases.deleteIndicator(indicatorId);
        return ResponseEntity.noContent().build();
    }

    private String evaluatorModelForTemplate(UUID templateId) {
        Template template = getTemplateUseCase.getById(templateId);
        return ProcessEnrichmentHelper.resolveEvaluatorModel(template.getType());
    }

    private List<TemplateLevel1Node> loadSortedTemplateTree(UUID templateId) {
        List<TemplateLevel1Node> level1Nodes = normativeHierarchyQueryPort.findTemplateTree(templateId)
                .map(tree -> new ArrayList<>(tree.level1Nodes()))
                .orElseGet(ArrayList::new);
        ProcessEnrichmentHelper.sortTemplateNormativeTree(level1Nodes);
        return level1Nodes;
    }
}
