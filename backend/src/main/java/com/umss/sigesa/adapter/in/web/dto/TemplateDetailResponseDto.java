package com.umss.sigesa.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailResponseDto {

    private UUID id;
    private String name;
    private String description;
    private String type;
    private String status;
    private int phaseCount;
    private int subphaseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TemplatePhaseResponseDto> phases;
}
