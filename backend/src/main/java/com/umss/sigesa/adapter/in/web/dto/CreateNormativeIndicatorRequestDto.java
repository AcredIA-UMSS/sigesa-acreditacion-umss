package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateNormativeIndicatorRequestDto {
    @NotBlank
    private String code;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal weight;

    @NotNull
    private Integer order;

    @NotBlank
    private String referenceUrl;
}
