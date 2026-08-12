package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePhaseRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Integer order;

    private String description;
}
