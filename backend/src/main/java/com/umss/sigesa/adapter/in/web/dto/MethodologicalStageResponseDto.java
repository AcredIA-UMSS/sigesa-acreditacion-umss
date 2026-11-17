package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class MethodologicalStageResponseDto {
    private UUID id;
    private UUID processId;
    private Integer order;
    private String code;
    private String displayName;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime closedAt;
    private List<StageDeliverableResponseDto> deliverables;
}
