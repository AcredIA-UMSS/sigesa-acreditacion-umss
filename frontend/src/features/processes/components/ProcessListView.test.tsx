import { http, HttpResponse } from 'msw';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ProcessListView } from './ProcessListView';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';

describe('ProcessListView', () => {
  it('shouldLoadProcessesFromApi', async () => {
    seedSession('CC');
    renderWithProviders(<ProcessListView />, { route: '/procesos' });

    expect(await screen.findByText('Ingeniería de Sistemas')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Procesos de acreditación' })).toBeInTheDocument();
    expect(screen.queryByText('Nuevo proceso')).not.toBeInTheDocument();
  });

  it('shouldShowCreateActionForJd', async () => {
    seedSession('JD');
    renderWithProviders(<ProcessListView />, { route: '/procesos' });
    expect(await screen.findByRole('link', { name: /Nuevo proceso/ })).toHaveAttribute(
      'href',
      '/procesos/nuevo',
    );
  });

  it('shouldShowApiError', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/processes', () =>
        HttpResponse.json({ error: 'UNKNOWN_ERROR', message: 'Listado no disponible' }, { status: 500 }),
      ),
    );
    renderWithProviders(<ProcessListView />, { route: '/procesos' });
    expect(await screen.findByText('Listado no disponible')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument();
  });
});
