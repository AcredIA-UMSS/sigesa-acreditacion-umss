package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ProcessResponseDto {
    private UUID id;
    private UUID careerId;
    private String careerCode;
    private String careerName;
    private UUID templateId;
    private String templateName;
    private String templateType;
    private String evaluatorModel;
    private String status;
    private LocalDateTime startDate;
    private List<PhaseDto> phases;
    private List<NormativeLevel1NodeDto> level1Nodes;
    private ProcessResponsibleDto responsible;

    @Getter @Builder
    public static class PhaseDto {
        private UUID id;
        private String name;
        private Integer order;
        private String description;
        private String status;
        private List<SubphaseDto> subphases;
    }

    @Getter @Builder
    public static class SubphaseDto {
        private UUID id;
        private String name;
        private Integer order;
        private String referenceUrl;
        private String description;
        private String requirements;
        private String status;
    }
}
