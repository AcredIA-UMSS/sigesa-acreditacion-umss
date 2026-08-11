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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertTemplateRequestDto {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String type;

    @NotEmpty
    @Valid
    private List<TemplatePhaseRequestDto> phases;
}
