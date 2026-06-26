package com.umss.sigesa.domain.model;

import java.util.UUID;

public class Template {
    private final UUID id;
    private final boolean validated;
    private final Taxonomy taxonomy;

    public Template(UUID id, boolean validated, Taxonomy taxonomy) {
        this.id = id;
        this.validated = validated;
        this.taxonomy = taxonomy;
    }

    public UUID getId() { return id; }
    public boolean isValidated() { return validated; }
    public Taxonomy getTaxonomy() { return taxonomy; }
}
