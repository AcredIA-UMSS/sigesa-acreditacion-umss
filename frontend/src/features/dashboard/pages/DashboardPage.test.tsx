import { http, HttpResponse } from 'msw';
import { screen, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DashboardPage } from './DashboardPage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';
import {
  dashboardSummaryJd,
  dashboardSummaryTd,
} from '../../../test/mocks/handlers';

describe('DashboardPage', () => {
  it('shouldRenderCoordinatorIndicatorsFromApi', async () => {
    seedSession('CC');
    renderWithProviders(<DashboardPage />, { route: '/dashboard' });

    expect(await screen.findByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('Indicadores')).toBeInTheDocument();
    expect(screen.getByText('48')).toBeInTheDocument();
    expect(screen.getByText('Progreso de Fases del Proceso')).toBeInTheDocument();
  });

  it('shouldRenderTechnicianSectionForTd', async () => {
    seedSession('TD');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () => HttpResponse.json(dashboardSummaryTd)),
    );
    renderWithProviders(<DashboardPage />, { route: '/dashboard' });

    expect(await screen.findByRole('heading', { name: 'Bandeja Técnica del Comité' })).toBeInTheDocument();
    expect(screen.getByText('Evidencias Pendientes')).toBeInTheDocument();
    expect(screen.getByText('28')).toBeInTheDocument();
  });

  it('shouldRenderExecutiveSectionForJd', async () => {
    seedSession('JD');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () => HttpResponse.json(dashboardSummaryJd)),
    );
    renderWithProviders(<DashboardPage />, { route: '/dashboard' });

    expect(
      await screen.findByRole('heading', { name: 'Panel de Acreditación Ejecutiva' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Generar reporte PDF/ })).toBeInTheDocument();
  });

  it('shouldShowApiErrorAndAllowRetry', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({ error: 'FORBIDDEN', message: 'Sin permisos PBAC' }, { status: 403 }),
      ),
    );
    renderWithProviders(<DashboardPage />, { route: '/dashboard' });

    expect(await screen.findByRole('heading', { name: 'Error al Cargar Dashboard' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument();
  });

  it('shouldShowRestrictedAccessWhenApiReturnsNoSections', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({
          userId: 'usr-empty',
          grantedPermissions: [],
        }),
      ),
    );
    renderWithProviders(<DashboardPage />, { route: '/dashboard' });

    expect(await screen.findByRole('heading', { name: 'Acceso Restringido' })).toBeInTheDocument();
  });

  it('shouldShowLoadingStateBeforeSummaryArrives', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', async () => {
        await new Promise((resolve) => setTimeout(resolve, 50));
        return HttpResponse.json({
          userId: 'usr-cc-sistemas-01',
          grantedPermissions: ['READ_CC_DASHBOARD'],
          coordinatorSection: {
            programId: 'p',
            programName: 'Ingeniería de Sistemas',
            totalIndicators: 1,
            overallProgressPercentage: 10,
            approvedEvidences: 0,
            rejectedEvidences: 0,
            pendingObservations: 0,
            phaseProgressList: [],
            bottlenecks: [],
          },
        });
      }),
    );

    renderWithProviders(<DashboardPage />, { route: '/dashboard' });
    expect(document.querySelector('.animate-pulse')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getAllByText('Ingeniería de Sistemas').length).toBeGreaterThan(0);
    });
  });
});
