package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private String period;
    private ProcessType type;
    private ProcessStatus status;
    private String taxonomySnapshotVersion;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    
    @Builder.Default
    private List<Phase> phases = new ArrayList<>();

    // Lógica de Dominio Puro: Inicialización a partir de una plantilla
    public static AccreditationProcess createFromTemplate(UUID careerId, Template template) {
        return createFromTemplate(careerId, template, null, null);
    }

    public static AccreditationProcess createFromTemplate(UUID careerId, Template template, String period, ProcessType type) {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(UUID.randomUUID())
                .careerId(careerId)
                .templateId(template.getId())
                .status(ProcessStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .period(period != null ? period : template.getActivePeriod())
                .type(type != null ? type : (template.getType() != null ? ProcessType.valueOf(template.getType()) : ProcessType.CEUB))
                .taxonomySnapshotVersion(template.getTaxonomy() != null ? template.getTaxonomy().version() : "1.0")
                .phases(new ArrayList<>())
                .build();

        if (template.getPhases() != null) {
            template.getPhases().forEach(tPhase -> {
                Phase phase = Phase.builder()
                        .id(UUID.randomUUID())
                        .name(tPhase.getName())
                        .order(tPhase.getOrder())
                        .subphases(new ArrayList<>())
                        .build();

                if (tPhase.getSubphases() != null) {
                    tPhase.getSubphases().forEach(tSubphase -> {
                        phase.getSubphases().add(Subphase.builder()
                                .id(UUID.randomUUID())
                                .name(tSubphase.getName())
                                .order(tSubphase.getOrder())
                                .build());
                    });
                }

                process.getPhases().add(phase);
            });
        }

        return process;
    }
}