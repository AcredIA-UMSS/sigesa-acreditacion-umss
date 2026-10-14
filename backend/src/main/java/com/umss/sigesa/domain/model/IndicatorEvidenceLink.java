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
public class IndicatorEvidenceLink {

    private UUID id;
    private UUID indicatorId;
    private UUID documentAssetId;
    private UUID evidenceId;
    private boolean primaryReference;
    private LocalDateTime createdAt;

    public static IndicatorEvidenceLink primary(
            UUID indicatorId,
            UUID documentAssetId,
            UUID evidenceId,
            LocalDateTime createdAt) {
        return IndicatorEvidenceLink.builder()
                .id(UUID.randomUUID())
                .indicatorId(indicatorId)
                .documentAssetId(documentAssetId)
                .evidenceId(evidenceId)
                .primaryReference(true)
                .createdAt(createdAt)
                .build();
    }
}
