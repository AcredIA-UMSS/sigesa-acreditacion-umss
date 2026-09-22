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
public class AccreditationProcess {
    private UUID id;
    private UUID careerId;
    private UUID templateId;
    private String status;
    private LocalDateTime startDate;

    public static AccreditationProcess createFromTemplate(UUID careerId, Template template) {
        return AccreditationProcess.builder()
                .careerId(careerId)
                .templateId(template.getId())
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .build();
    }
}
