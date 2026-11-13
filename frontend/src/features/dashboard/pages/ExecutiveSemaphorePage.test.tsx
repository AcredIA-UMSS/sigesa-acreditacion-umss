import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ExecutiveSemaphorePage } from './ExecutiveSemaphorePage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';

function stubCommonApis() {
  server.use(
    http.get('/api/v1/notifications/me', () =>
      HttpResponse.json({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 15,
        number: 0,
      }),
    ),
    http.get('/api/v1/processes', () => HttpResponse.json([])),
  );
}

describe('ExecutiveSemaphorePage', () => {
  it('shows faculty groups, a red career and a career without data', async () => {
    seedSession('JD');
    stubCommonApis();
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
                processId: 'proc-sistemas-1',
              },
            ],
          },
        }),
      ),
    );

    renderWithProviders(<ExecutiveSemaphorePage />, { route: '/executive/semaphore' });

    expect(await screen.findByRole('heading', { name: 'Semáforo institucional' })).toBeInTheDocument();
    expect(await screen.findByPlaceholderText('Buscar carrera o facultad')).toBeInTheDocument();
    expect(screen.getAllByText('Facultad de Ciencias y Tecnología').length).toBeGreaterThan(0);
    expect(screen.getByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('Ingeniería Civil')).toBeInTheDocument();
    expect(screen.getAllByText('Crítico').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Sin dato').length).toBeGreaterThan(0);
    expect(screen.getAllByText('40%').length).toBeGreaterThan(0);
  });

  it('navigates to the process when a program row with processId is clicked', async () => {
    seedSession('JD');
    stubCommonApis();
    const user = userEvent.setup();

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
                programId: 'sistemas',
                name: 'Ingeniería de Sistemas',
                faculty: 'FCT',
                status: 'ROJO',
                criticalObservations: 2,
                completionPercentage: 40,
                processId: 'proc-sistemas-1',
              },
            ],
          },
        }),
      ),
    );

    renderWithProviders(
      <Routes>
        <Route path="/executive/semaphore" element={<ExecutiveSemaphorePage />} />
        <Route path="/procesos/:processId" element={<h1>Detalle de proceso</h1>} />
      </Routes>,
      { route: '/executive/semaphore' },
    );

    await user.click(await screen.findByText('Ingeniería de Sistemas'));
    expect(await screen.findByRole('heading', { name: 'Detalle de proceso' })).toBeInTheDocument();
  });
});
