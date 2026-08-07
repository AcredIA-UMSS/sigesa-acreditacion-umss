package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProcessResponsibleDto {
    private UUID userId;
    private String fullName;
    private String email;
    private LocalDateTime assignedAt;
}
