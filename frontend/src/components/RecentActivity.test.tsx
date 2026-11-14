import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { seedSession } from '../test/session';
import { renderWithProviders } from '../test/test-utils';
import { server } from '../test/mocks/server';
import { MOCK_RECENT_ACTIVITIES, RecentActivity } from './RecentActivity';

describe('RecentActivity', () => {
  it('shouldRenderProcessDataFromApi', async () => {
    seedSession('CC');
    renderWithProviders(<RecentActivity />, { route: '/dashboard' });

    expect(await screen.findByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByText('Activo')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Últimas actividades' })).toBeInTheDocument();
  });

  it('shouldFallbackToMockDataWhenApiFails', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/processes', () =>
        HttpResponse.json({ error: 'UNKNOWN_ERROR', message: 'Listado no disponible' }, { status: 500 }),
      ),
    );
    renderWithProviders(<RecentActivity />, { route: '/dashboard' });

    expect(await screen.findByText(MOCK_RECENT_ACTIVITIES[0].name)).toBeInTheDocument();
    expect(screen.getByText(MOCK_RECENT_ACTIVITIES[0].status)).toBeInTheDocument();
    expect(screen.getByText(MOCK_RECENT_ACTIVITIES[0].date)).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(5);
  });
});
