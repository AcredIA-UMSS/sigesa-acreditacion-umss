package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubphaseRequestDto {

    private String name;
    private Integer order;
    private String referenceUrl;
    private String description;
    private String requirements;
}
