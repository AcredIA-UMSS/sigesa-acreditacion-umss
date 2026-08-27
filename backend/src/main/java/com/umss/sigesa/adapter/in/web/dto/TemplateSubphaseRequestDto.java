package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSubphaseRequestDto {

    private UUID id;

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
