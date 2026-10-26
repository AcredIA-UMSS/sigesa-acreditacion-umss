import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ExecutiveSemaphorePage } from './ExecutiveSemaphorePage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';

describe('ExecutiveSemaphorePage', () => {
  it('shows faculty groups, a red career and a career without data', async () => {
    seedSession('JD');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({
          userId: 'usr-jd',
          grantedPermissions: ['ROLE_JD'],
          executiveSection: {
            totalPrograms: 1,
            averageGlobalProgress: 40,
            criticalObservations: 2,
            alertPrograms: 1,
            programTrafficLights: [
              {
                programId: 'civil',
                name: 'Ingeniería Civil',
                faculty: 'FCT',
                status: 'SIN_DATO',
                criticalObservations: 0,
              },
              {
                programId: 'sistemas',
                name: 'Ingeniería de Sistemas',
                faculty: 'FCT',
                status: 'ROJO',
                criticalObservations: 2,
                completionPercentage: 40,
              },
            ],
          },
        }),
      ),
    );

    renderWithProviders(<ExecutiveSemaphorePage />, { route: '/executive/semaphore' });

    expect(await screen.findByRole('heading', { name: 'Semáforo institucional' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'FCT' })).toBeInTheDocument();
    expect(screen.getByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('Ingeniería Civil')).toBeInTheDocument();
    expect(screen.getAllByText('Crítico').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Sin dato').length).toBeGreaterThan(0);
    expect(screen.getByText('40%')).toBeInTheDocument();
  });
});
