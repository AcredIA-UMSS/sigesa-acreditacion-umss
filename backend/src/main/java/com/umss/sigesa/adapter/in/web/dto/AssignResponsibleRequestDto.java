package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignResponsibleRequestDto {

    @NotNull
    private UUID userId;
}
