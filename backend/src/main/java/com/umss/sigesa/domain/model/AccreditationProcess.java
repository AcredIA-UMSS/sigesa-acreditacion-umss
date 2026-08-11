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
    private String status;
    private LocalDateTime startDate;
    @Builder.Default
    private List<Phase> phases = new ArrayList<>();

    // Lógica de Dominio Puro: Inicialización a partir de una plantilla
    public static AccreditationProcess createFromTemplate(UUID careerId, Template template) {
        AccreditationProcess process = AccreditationProcess.builder()
                .careerId(careerId)
                .templateId(template.getId())
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .phases(new ArrayList<>())
                .build();

        template.getPhases().forEach(tPhase -> {
            Phase phase = Phase.builder()
                    .name(tPhase.getName())
                    .order(tPhase.getOrder())
                    .description(tPhase.getDescription())
                    .subphases(new ArrayList<>())
                    .build();

            tPhase.getSubphases().forEach(tSubphase -> {
                phase.getSubphases().add(Subphase.builder()
                        .name(tSubphase.getName())
                        .order(tSubphase.getOrder())
                        .referenceUrl(tSubphase.getReferenceUrl())
                        .description(tSubphase.getDescription())
                        .build());
            });

            process.getPhases().add(phase);
        });

        return process;
    }
}