import type { CreateProcessRequestType } from '../../../api/model/createProcessRequestType';

/**
 * UUIDs alineados con `DevSeedData.java` (plantillas validadas en seed de desarrollo).
 * No hay GET /templates en el backend; estos IDs son la fuente verificable disponible.
 */
export interface SeedTemplateOption {
  id: string;
  label: string;
  type: CreateProcessRequestType;
  taxonomyVersion: string;
}

export const SEED_VALIDATED_TEMPLATES: SeedTemplateOption[] = [
  {
    id: '850e8400-e29b-41d4-a716-446655440010',
    label: 'Modelo Nacional CEUB 2026 (CEUB-2026.1)',
    type: 'CEUB',
    taxonomyVersion: 'CEUB-2026.1',
  },
  {
    id: '850e8400-e29b-41d4-a716-446655440011',
    label: 'Modelo Regional ARCU-SUR 2026 (ARCU-SUR-2026.1)',
    type: 'ARCU_SUR',
    taxonomyVersion: 'ARCU-SUR-2026.1',
  },
];

/** Periodos usados en seed de procesos (`DevSeedData.PERIOD_*`). */
export const SEED_PERIODS = ['2026-1', '2025-2'] as const;
