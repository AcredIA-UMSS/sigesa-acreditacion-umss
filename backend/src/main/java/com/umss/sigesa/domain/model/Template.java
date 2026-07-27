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
public class Template {
    private UUID id;
    private String name;
    private String type;
    private boolean validated;
    private Taxonomy taxonomy;
    private String activePeriod;
    private LocalDateTime activatedAt;

    @Builder.Default
    private List<TemplatePhase> phases = new ArrayList<>();

    public Template(UUID id, boolean validated, Taxonomy taxonomy) {
        this.id = id;
        this.validated = validated;
        this.taxonomy = taxonomy;
        this.phases = new ArrayList<>();
    }

    public Template(UUID id, boolean validated, Taxonomy taxonomy, String activePeriod, LocalDateTime activatedAt) {
        this.id = id;
        this.validated = validated;
        this.taxonomy = taxonomy;
        this.activePeriod = activePeriod;
        this.activatedAt = activatedAt;
        this.phases = new ArrayList<>();
    }

    public Template withActivation(String period, LocalDateTime activatedAt) {
        return Template.builder()
                .id(this.id)
                .name(this.name)
                .type(this.type)
                .validated(this.validated)
                .taxonomy(this.taxonomy)
                .activePeriod(period)
                .activatedAt(activatedAt)
                .phases(this.phases)
                .build();
    }
}
