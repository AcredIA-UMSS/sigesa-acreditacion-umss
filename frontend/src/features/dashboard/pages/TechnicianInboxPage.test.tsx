import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { TechnicianInboxPage } from './TechnicianInboxPage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';

const mockInboxResponse = {
  content: [
    {
      indicatorId: '00000000-0000-0000-0000-000000000001',
      indicatorCode: 'IND-1.1.2',
      indicatorDescription: 'Reglamento de docencia actualizado',
      status: 'SUBIDO',
      dimensionName: 'Dimensión 1: Institucional',
      criterionName: 'Criterio 1.1: Normativa',
      subcriterionName: 'Subcriterio 1.1.1',
      pathHierarchy: 'Dimensión 1 > Criterio 1.1 > Subcriterio 1.1.1',
      programId: '11111111-1111-1111-1111-111111111111',
      programName: 'Carrera Informática',
      evidenceId: '22222222-2222-2222-2222-222222222222',
      evidenceFilename: 'reglamento_docencia_2026.pdf',
      evidenceUploadedAt: '2026-10-18T10:00:00',
      activeObservationsCount: 0,
      updatedAt: '2026-10-18T10:00:00',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 20,
  number: 0,
};

describe('TechnicianInboxPage', () => {
  it('renders technician inbox table and items from API', async () => {
    seedSession('TD');
    server.use(
      http.get('/api/v1/dashboards/technician/inbox', () =>
        HttpResponse.json(mockInboxResponse),
      ),
    );

    renderWithProviders(<TechnicianInboxPage />, { route: '/technician/inbox' });

    expect(
      await screen.findByText('Bandeja de dictamen'),
    ).toBeInTheDocument();
    expect(await screen.findByText('IND-1.1.2')).toBeInTheDocument();
    expect(screen.getByText('reglamento_docencia_2026.pdf')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Aprobar/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Observar/i })).toBeInTheDocument();
  });

  it('opens approval modal when clicking Aprobar', async () => {
    const user = userEvent.setup();
    seedSession('TD');
    server.use(
      http.get('/api/v1/dashboards/technician/inbox', () =>
        HttpResponse.json(mockInboxResponse),
      ),
      http.post('/api/v1/indicators/:id/approve', () =>
        HttpResponse.json({ status: 'APROBADO' }),
      ),
    );

    renderWithProviders(<TechnicianInboxPage />, { route: '/technician/inbox' });

    const approveButton = await screen.findByRole('button', { name: /Aprobar/i });
    await user.click(approveButton);

    expect(
      await screen.findByText('Confirmar Aprobación Técnica'),
    ).toBeInTheDocument();
  });
});
