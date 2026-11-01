import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { AuditLogPage } from './AuditLogPage';
import { seedSession } from '../../../../test/session';
import { renderWithProviders } from '../../../../test/test-utils';
import { server } from '../../../../test/mocks/server';

const mockAuditLogsResponse = {
  content: [
    {
      id: '00000000-0000-0000-0000-000000000099',
      actorId: '11111111-1111-1111-1111-111111111111',
      actorEmail: 'jefatura@umss.edu.bo',
      actorRole: 'JD',
      action: 'STAGE_GATE_APPROVED',
      entityType: 'STAGE',
      entityId: 'STAGE-E2',
      payloadDiff: 'Aprobación formal de compuerta metodológica E2',
      ipAddress: '192.168.1.15',
      createdAt: '2026-10-18T14:30:00',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 20,
  number: 0,
};

describe('AuditLogPage', () => {
  it('renders audit log page and displays immutable logs from API', async () => {
    seedSession('JD');
    server.use(
      http.get('/api/v1/audit/logs', () =>
        HttpResponse.json(mockAuditLogsResponse),
      ),
    );

    renderWithProviders(<AuditLogPage />, { route: '/admin/auditoria' });

    expect(
      await screen.findByText('Bitácora Inmutable de Auditoría [JD]'),
    ).toBeInTheDocument();
    expect(await screen.findByText('jefatura@umss.edu.bo')).toBeInTheDocument();
    expect(screen.getByText('STAGE_GATE_APPROVED')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Exportar CSV/i })).toBeInTheDocument();
  });
});
