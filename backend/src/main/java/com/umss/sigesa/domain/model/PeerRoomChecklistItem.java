package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerRoomChecklistItem {

    public static final List<CatalogEntry> CATALOG = List.of(
            new CatalogEntry("DOCUMENTATION_PACKAGE", "Paquete documental de pares"),
            new CatalogEntry("EVIDENCE_SAMPLE", "Muestra de evidencias"),
            new CatalogEntry("INTERVIEW_GUIDE", "Guía de entrevistas"),
            new CatalogEntry("LOGISTICS", "Logística de visita")
    );

    private UUID id;
    private UUID processId;
    private String itemCode;
    private String label;
    private boolean checked;
    private UUID checkedBy;
    private LocalDateTime checkedAt;

    public record CatalogEntry(String itemCode, String label) {
    }

    public static PeerRoomChecklistItem seed(UUID processId, CatalogEntry catalog, LocalDateTime now) {
        return PeerRoomChecklistItem.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .itemCode(catalog.itemCode())
                .label(catalog.label())
                .checked(false)
                .build();
    }
}
