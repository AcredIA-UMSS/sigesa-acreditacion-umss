package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class NormativeLevel1NodeDto {
    private UUID id;
    private String name;
    private String label;
    private Integer order;
    private String description;
    private String status;
    private List<NormativeLevel2NodeDto> level2Nodes;
}
