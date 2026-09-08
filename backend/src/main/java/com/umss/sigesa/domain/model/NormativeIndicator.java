package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormativeIndicator {
    private UUID id;
    private String code;
    private String description;
    private BigDecimal weight;
    private Integer order;
    private String referenceUrl;
    private IndicatorState status;
    private UUID legacySubphaseId;
}
