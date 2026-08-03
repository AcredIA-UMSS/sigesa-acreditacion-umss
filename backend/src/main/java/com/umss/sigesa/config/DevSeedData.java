package com.umss.sigesa.config;

import java.util.UUID;

/**
 * Identificadores estables de datos de demostración para desarrollo local.
 * Referenciados por loaders, catálogo y documentación (README).
 */
public final class DevSeedData {

    private DevSeedData() {
    }

    // --- Carreras (catálogo programs) ---

    public static final UUID PROGRAM_INF_SIS = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    public static final UUID PROGRAM_ING_CIVIL = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID PROGRAM_ING_QUIMICA = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID PROGRAM_ING_INDUSTRIAL = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    public static final UUID PROGRAM_ING_ELECTRONICA = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
    public static final UUID PROGRAM_ING_MECANICA = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
    public static final UUID PROGRAM_ING_AMBIENTAL = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
    public static final UUID PROGRAM_ING_PETROLEO = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");
    public static final UUID PROGRAM_ING_MINAS = UUID.fromString("550e8400-e29b-41d4-a716-446655440008");
    public static final UUID PROGRAM_ING_TELECOM = UUID.fromString("550e8400-e29b-41d4-a716-446655440009");
    public static final UUID PROGRAM_ARQUITECTURA = UUID.fromString("550e8400-e29b-41d4-a716-44665544000a");
    public static final UUID PROGRAM_MEDICINA = UUID.fromString("550e8400-e29b-41d4-a716-44665544000b");
    public static final UUID PROGRAM_ENFERMERIA = UUID.fromString("550e8400-e29b-41d4-a716-44665544000c");
    public static final UUID PROGRAM_MED_VET = UUID.fromString("550e8400-e29b-41d4-a716-44665544000d");
    public static final UUID PROGRAM_DERECHO = UUID.fromString("550e8400-e29b-41d4-a716-44665544000e");
    public static final UUID PROGRAM_CONTADURIA = UUID.fromString("550e8400-e29b-41d4-a716-44665544000f");
    public static final UUID PROGRAM_ADM_EMPRESAS = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");
    public static final UUID PROGRAM_ECONOMIA = UUID.fromString("550e8400-e29b-41d4-a716-446655440011");
    public static final UUID PROGRAM_PSICOLOGIA = UUID.fromString("550e8400-e29b-41d4-a716-446655440012");
    public static final UUID PROGRAM_SOCIOLOGIA = UUID.fromString("550e8400-e29b-41d4-a716-446655440013");
    public static final UUID PROGRAM_COM_SOCIAL = UUID.fromString("550e8400-e29b-41d4-a716-446655440014");
    public static final UUID PROGRAM_TRABAJO_SOCIAL = UUID.fromString("550e8400-e29b-41d4-a716-446655440015");
    public static final UUID PROGRAM_BIOLOGIA = UUID.fromString("550e8400-e29b-41d4-a716-446655440016");
    public static final UUID PROGRAM_ING_AGRONOMICA = UUID.fromString("550e8400-e29b-41d4-a716-446655440017");
    public static final UUID PROGRAM_ING_ALIMENTOS = UUID.fromString("550e8400-e29b-41d4-a716-446655440018");

    /** @deprecated Usar constantes {@code PROGRAM_*} del catálogo de carreras. */
    @Deprecated
    public static final UUID PROGRAM_CEUB = PROGRAM_ING_CIVIL;

    /** @deprecated Usar constantes {@code PROGRAM_*} del catálogo de carreras. */
    @Deprecated
    public static final UUID PROGRAM_ARCUSUR = PROGRAM_MEDICINA;

    // --- Plantillas (solo CEUB y ARCU-SUR) ---

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
