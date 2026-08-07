package com.umss.sigesa.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePhaseResponseDto {

    private UUID id;
    private String name;
    private Integer order;
    private String description;
    private List<TemplateSubphaseResponseDto> subphases;
}
