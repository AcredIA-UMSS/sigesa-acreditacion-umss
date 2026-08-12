package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class TemplatePhaseRequestDto {

    private UUID id;

    @NotBlank
    private String name;

    @NotNull
    private Integer order;

    private String description;

    @NotEmpty
    @Valid
    private List<TemplateSubphaseRequestDto> subphases;
}
