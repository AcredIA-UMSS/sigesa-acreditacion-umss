package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodologicalStage {
    private UUID id;
    private UUID processId;
    private int order;
    private MethodologicalStageCode code;
    private MethodologicalStageStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime closedAt;
    @Builder.Default
    private List<StageDeliverable> deliverables = new ArrayList<>();
}
