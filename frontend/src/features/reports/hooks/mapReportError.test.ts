import { describe, expect, it } from 'vitest';
import { mapReportError } from './mapReportError';

describe('mapReportError', () => {
  it('shouldTranslateKnownJobCodes', () => {
    expect(mapReportError('REPORT_NOT_READY')).toContain('aún no está listo');
    expect(mapReportError('Download failed: REPORT_NOT_READY')).toContain('aún no está listo');
    expect(mapReportError(null)).toBeNull();
  });
});
