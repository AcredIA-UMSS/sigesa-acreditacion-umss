/** IDs alineados con {@link DevSeedData} en el backend. */
export const SEED_TEMPLATE_CEUB_ID = '850e8400-e29b-41d4-a716-446655440010';
export const SEED_TEMPLATE_ARCUSUR_ID = '850e8400-e29b-41d4-a716-446655440011';

export const SEED_TEMPLATES = [
  { id: SEED_TEMPLATE_CEUB_ID, name: 'CEUB 2026', type: 'CEUB' },
  { id: SEED_TEMPLATE_ARCUSUR_ID, name: 'ARCU-SUR 2026', type: 'ARCU-SUR' },
] as const;
