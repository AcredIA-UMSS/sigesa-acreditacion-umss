package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class NormativeIndicatorDetailResponseDto {
    private UUID id;
    private UUID processId;
    private UUID careerId;
    private UUID level1Id;
    private String level1Name;
    private String code;
    private String description;
    private BigDecimal weight;
    private Integer order;
    private String status;
    private String referenceUrl;
    private List<String> normativePath;
}
