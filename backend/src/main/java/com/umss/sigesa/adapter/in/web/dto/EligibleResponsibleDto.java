package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EligibleResponsibleDto {
    private UUID userId;
    private String fullName;
    private String email;
}
