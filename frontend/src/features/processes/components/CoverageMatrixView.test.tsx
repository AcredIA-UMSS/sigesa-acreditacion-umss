import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '../../../test/test-utils';
import { CoverageMatrixView } from './CoverageMatrixView';

describe('CoverageMatrixView', () => {
  it('shows criterion, indicator, evidence count and status', async () => {
    const onDownloadPdf = vi.fn();
    renderWithProviders(
      <CoverageMatrixView
        processId="proc-1"
        isLoading={false}
        errorMessage={null}
        isDownloading={false}
        onDownloadPdf={onDownloadPdf}
        matrix={{
          processId: 'proc-1',
          careerName: 'Ingeniería de Sistemas',
          totals: {
            indicators: 1,
            withEvidence: 1,
            approved: 1,
            observed: 0,
            other: 0,
            coveragePercent: 100,
          },
          dimensions: [
            {
              id: 'd1',
              name: 'Contexto institucional',
              components: [
                {
                  id: 'c1',
                  name: 'Misión',
                  criteria: [
                    {
                      id: 'cr1',
                      name: 'Criterio 1.1',
                      indicators: [
                        {
                          id: 'i1',
                          code: 'IND-1.1.1',
                          description: 'Misión publicada',
                          status: 'APROBADO',
                          evidenceCount: 2,
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        }}
      />,
      { route: '/procesos/proc-1/cobertura' },
    );

    expect(screen.getByRole('heading', { name: 'Matriz de cobertura' })).toBeInTheDocument();
    expect(screen.getByText('Criterio 1.1')).toBeInTheDocument();
    expect(screen.getByText('IND-1.1.1')).toBeInTheDocument();
    expect(screen.getByText('APROBADO')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /Descargar PDF/i }));
    expect(onDownloadPdf).toHaveBeenCalledOnce();
  });
});
