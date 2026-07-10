package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Template {
    private final UUID id;
    private final boolean validated;
    private final Taxonomy taxonomy;
    private final String activePeriod;
    private final LocalDateTime activatedAt;

    public Template(UUID id, boolean validated, Taxonomy taxonomy) {
        this(id, validated, taxonomy, null, null);
    }

    public Template(UUID id,
                    boolean validated,
                    Taxonomy taxonomy,
                    String activePeriod,
                    LocalDateTime activatedAt) {
        this.id = id;
        this.validated = validated;
        this.taxonomy = taxonomy;
        this.activePeriod = activePeriod;
        this.activatedAt = activatedAt;
    }

    public UUID getId() {
        return id;
    }

    public boolean isValidated() {
        return validated;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public String getActivePeriod() {
        return activePeriod;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public Template withActivation(String period, LocalDateTime activatedAt) {
        return new Template(id, validated, taxonomy, period, activatedAt);
    }
}
