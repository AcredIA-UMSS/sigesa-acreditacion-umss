package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectIndicatorRequestDto {

    @NotBlank
    @Size(min = 20, max = 4000)
    private String justification;
}
