package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private TemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
