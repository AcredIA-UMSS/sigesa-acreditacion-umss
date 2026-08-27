package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubphaseRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Integer order;

    @NotBlank
    private String referenceUrl;

    private String description;

    @NotBlank
    private String requirements;
}
