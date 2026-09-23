package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateNormativeIndicatorRequestDto {
    private String code;
    private String description;
    private BigDecimal weight;
    private Integer order;
    private String referenceUrl;
}
