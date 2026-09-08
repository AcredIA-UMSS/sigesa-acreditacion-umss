package com.umss.sigesa.adapter.in.web.mapper;

import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorDetailResponseDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeIndicatorDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel1NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel2NodeDto;
import com.umss.sigesa.adapter.in.web.dto.NormativeLevel3NodeDto;
import com.umss.sigesa.application.model.process.NormativeIndicatorDetail;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NormativeStructureWebMapper {

    public List<NormativeLevel1NodeDto> toLevel1DtoList(List<Level1Node> nodes, String evaluatorModel) {
        if (nodes == null) {
            return List.of();
        }
        return nodes.stream().map(node -> toLevel1Dto(node, evaluatorModel)).toList();
    }

    public NormativeLevel1NodeDto toLevel1Dto(Level1Node node, String evaluatorModel) {
        return NormativeLevel1NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level1Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .status(node.getStatus() != null ? node.getStatus().name() : "ABIERTA")
                .level2Nodes(node.getLevel2Nodes() == null ? List.of() : node.getLevel2Nodes().stream()
                        .map(level2 -> toLevel2Dto(level2, evaluatorModel))
                        .toList())
                .build();
    }

    public NormativeLevel2NodeDto toLevel2Dto(Level2Node node, String evaluatorModel) {
        return NormativeLevel2NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level2Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .level3Nodes(node.getLevel3Nodes() == null ? List.of() : node.getLevel3Nodes().stream()
                        .map(level3 -> toLevel3Dto(level3, evaluatorModel))
                        .toList())
                .build();
    }

    public NormativeLevel3NodeDto toLevel3Dto(Level3Node node, String evaluatorModel) {
        return NormativeLevel3NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level3Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .indicators(node.getIndicators() == null ? List.of() : node.getIndicators().stream()
                        .map(this::toIndicatorDto)
                        .toList())
                .build();
    }

    public NormativeIndicatorDto toIndicatorDto(NormativeIndicator indicator) {
        return NormativeIndicatorDto.builder()
                .id(indicator.getId())
                .code(indicator.getCode())
                .description(indicator.getDescription())
                .weight(indicator.getWeight())
                .order(indicator.getOrder())
                .status(indicator.getStatus() != null ? indicator.getStatus().name() : "PENDIENTE")
                .referenceUrl(indicator.getReferenceUrl())
                .legacySubphaseId(indicator.getLegacySubphaseId())
                .build();
    }

    public NormativeIndicatorDetailResponseDto toIndicatorDetailDto(NormativeIndicatorDetail detail) {
        NormativeIndicator indicator = detail.indicator();
        return NormativeIndicatorDetailResponseDto.builder()
                .id(indicator.getId())
                .processId(detail.processId())
                .careerId(detail.careerId())
                .level1Id(detail.level1Id())
                .level1Name(detail.level1Name())
                .code(indicator.getCode())
                .description(indicator.getDescription())
                .weight(indicator.getWeight())
                .order(indicator.getOrder())
                .status(indicator.getStatus() != null ? indicator.getStatus().name() : "PENDIENTE")
                .referenceUrl(indicator.getReferenceUrl())
                .normativePath(detail.normativePath())
                .build();
    }

    public List<NormativeLevel1NodeDto> toTemplateLevel1DtoList(List<TemplateLevel1Node> nodes, String evaluatorModel) {
        if (nodes == null) {
            return List.of();
        }
        return nodes.stream().map(node -> toTemplateLevel1Dto(node, evaluatorModel)).toList();
    }

    public NormativeLevel1NodeDto toTemplateLevel1Dto(TemplateLevel1Node node, String evaluatorModel) {
        return NormativeLevel1NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level1Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .level2Nodes(node.getLevel2Nodes() == null ? List.of() : node.getLevel2Nodes().stream()
                        .map(level2 -> toTemplateLevel2Dto(level2, evaluatorModel))
                        .toList())
                .build();
    }

    public NormativeLevel2NodeDto toTemplateLevel2Dto(TemplateLevel2Node node, String evaluatorModel) {
        return NormativeLevel2NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level2Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .level3Nodes(node.getLevel3Nodes() == null ? List.of() : node.getLevel3Nodes().stream()
                        .map(level3 -> toTemplateLevel3Dto(level3, evaluatorModel))
                        .toList())
                .build();
    }

    public NormativeLevel3NodeDto toTemplateLevel3Dto(TemplateLevel3Node node, String evaluatorModel) {
        return NormativeLevel3NodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .label(NormativeLevelLabelResolver.level3Label(evaluatorModel))
                .order(node.getOrder())
                .description(node.getDescription())
                .indicators(node.getIndicators() == null ? List.of() : node.getIndicators().stream()
                        .map(this::toTemplateIndicatorDto)
                        .toList())
                .build();
    }

    public NormativeIndicatorDto toTemplateIndicatorDto(TemplateNormativeIndicator indicator) {
        return NormativeIndicatorDto.builder()
                .id(indicator.getId())
                .code(indicator.getCode())
                .description(indicator.getDescription())
                .weight(indicator.getWeight())
                .order(indicator.getOrder())
                .referenceUrl(indicator.getReferenceUrl())
                .build();
    }
}
