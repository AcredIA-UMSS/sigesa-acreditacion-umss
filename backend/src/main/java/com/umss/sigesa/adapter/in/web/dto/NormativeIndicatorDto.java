package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class NormativeIndicatorDto {
    private UUID id;
    private String code;
    private String description;
    private BigDecimal weight;
    private Integer order;
    private String status;
    private String referenceUrl;
    private UUID legacySubphaseId;
}
