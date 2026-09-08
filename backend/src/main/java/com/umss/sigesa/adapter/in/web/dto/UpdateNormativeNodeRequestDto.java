package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNormativeNodeRequestDto {
    private String name;
    private Integer order;
    private String description;
}
