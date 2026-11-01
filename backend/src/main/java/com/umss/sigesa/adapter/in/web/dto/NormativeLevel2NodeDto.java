package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class NormativeLevel2NodeDto {
    private UUID id;
    private String name;
    private String label;
    private Integer order;
    private String description;
    private List<NormativeLevel3NodeDto> level3Nodes;
}
