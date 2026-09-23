import { describe, expect, it } from 'vitest';
import { buildReportPreview } from './reportPreview';

describe('buildReportPreview', () => {
  it('shouldLabelAllFacultiesAndProgramsWhenFiltersAreEmpty', () => {
    const preview = buildReportPreview({
      facultyId: '',
      programId: '',
      managementYear: 2026,
    });
    expect(preview.title).toBe('Reporte Ejecutivo de Acreditación');
    expect(preview.facultyLabel).toBe('Todos');
    expect(preview.programLabel).toBe('Todos');
    expect(preview.managementYear).toBe(2026);
  });
});
