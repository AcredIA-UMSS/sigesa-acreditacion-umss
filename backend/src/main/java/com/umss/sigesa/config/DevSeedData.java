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

    // --- Indicadores y criterios (catálogo estático demo) ---

    public static final UUID INDICATOR_102 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440100");
    public static final UUID CRITERION_3_1 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440101");
    public static final UUID INDICATOR_201 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440110");
    public static final UUID CRITERION_2_1 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440111");
    public static final UUID INDICATOR_305 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440120");
    public static final UUID CRITERION_3_5 = UUID.fromString("a50e8400-e29b-41d4-a716-446655440121");

    // --- Evidencias demo (MOD-EVIDENCE seed) ---

    public static final UUID EVIDENCE_LABS = UUID.fromString("b50e8400-e29b-41d4-a716-446655440200");
    public static final UUID EVIDENCE_LABS_V1 = UUID.fromString("b50e8400-e29b-41d4-a716-446655440201");
    public static final UUID EVIDENCE_PLAN = UUID.fromString("b50e8400-e29b-41d4-a716-446655440210");
    public static final UUID EVIDENCE_PLAN_V1 = UUID.fromString("b50e8400-e29b-41d4-a716-446655440211");
    public static final UUID EVIDENCE_BIBLIO = UUID.fromString("b50e8400-e29b-41d4-a716-446655440220");
    public static final UUID EVIDENCE_BIBLIO_V1 = UUID.fromString("b50e8400-e29b-41d4-a716-446655440221");

    public static final String DEMO_EVIDENCE_STORAGE_LABS = "demo-evidence-labs.pdf";
    public static final String DEMO_EVIDENCE_STORAGE_PLAN = "demo-evidence-plan.pdf";
    public static final String DEMO_EVIDENCE_STORAGE_BIBLIO = "demo-evidence-biblio.pdf";
}
