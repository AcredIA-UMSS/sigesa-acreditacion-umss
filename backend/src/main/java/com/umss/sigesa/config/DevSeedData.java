package com.umss.sigesa.config;

import java.util.UUID;

/**
 * Identificadores estables de datos de demostración para desarrollo local.
 * Referenciados por loaders, catálogo estático y documentación (README).
 */
public final class DevSeedData {

    private DevSeedData() {
    }

    // --- Programas (catálogo estático) ---

    public static final UUID PROGRAM_INF_SIS = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    public static final UUID PROGRAM_CEUB = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    public static final UUID PROGRAM_ARCUSUR = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");

    // --- Plantillas ---

    public static final UUID TEMPLATE_CEUB_2026 = UUID.fromString("850e8400-e29b-41d4-a716-446655440010");
    public static final UUID TEMPLATE_ARCUSUR_2026 = UUID.fromString("850e8400-e29b-41d4-a716-446655440011");
    public static final UUID TEMPLATE_DRAFT = UUID.fromString("850e8400-e29b-41d4-a716-446655440012");

    public static final String TAXONOMY_CEUB_VERSION = "CEUB-2026.1";
    public static final String TAXONOMY_ARCUSUR_VERSION = "ARCU-SUR-2026.1";
    public static final String TAXONOMY_DRAFT_VERSION = "DRAFT-0.1";

    // --- Procesos de acreditación ---

    public static final UUID PROCESS_INF_SIS_CEUB_ACTIVE = UUID.fromString("950e8400-e29b-41d4-a716-446655440020");
    public static final UUID PROCESS_CEUB_CLOSED = UUID.fromString("950e8400-e29b-41d4-a716-446655440021");
    public static final UUID PROCESS_ARCUSUR_ARCHIVED = UUID.fromString("950e8400-e29b-41d4-a716-446655440022");

    public static final String PERIOD_2026_1 = "2026-1";
    public static final String PERIOD_2025_2 = "2025-2";
}
