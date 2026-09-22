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
    private List<NormativeLevel1NodeDto> level1Nodes;
    private ProcessResponsibleDto responsible;
}
