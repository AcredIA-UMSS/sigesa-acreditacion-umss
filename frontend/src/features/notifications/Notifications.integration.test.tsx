import { http, HttpResponse } from 'msw';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { describe, expect, it } from 'vitest';
import { createTestQueryClient } from '../../test/test-utils';
import { AuthProvider } from '../../lib/auth/AuthProvider';
import { seedSession } from '../../test/session';
import { server } from '../../test/mocks/server';
import { buildNotificationsPage, notificationFixtureRejected } from '../../test/mocks/handlers';
import App from '../../App';

function renderApp(path: string) {
  window.history.pushState({}, '', path);
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </QueryClientProvider>,
  );
}

function coordinatorSummary(overrides: Record<string, unknown> = {}) {
  return {
    userId: 'usr-cc',
    grantedPermissions: ['READ_CC_DASHBOARD'],
    coordinatorSection: {
      programId: 'prog-sistemas-umss',
      programName: 'Ingeniería de Sistemas',
      totalIndicators: 48,
      overallProgressPercentage: 72.5,
      approvedEvidences: 110,
      rejectedEvidences: 12,
      pendingObservations: 5,
      phaseProgressList: [],
      bottlenecks: [],
    },
    ...overrides,
  };
}

describe('Notifications feature integration', () => {
  it('renders the notifications page for an authenticated CC session', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({ userId: 'usr-cc', grantedPermissions: ['READ_CC_DASHBOARD'] }),
      ),
    );

    renderApp('/notificaciones');

    expect(
      await screen.findByRole('heading', { name: /^Notificaciones$/i }),
    ).toBeInTheDocument();
    expect(
      await screen.findByText('Indicador 1.2 con observaciones'),
    ).toBeInTheDocument();
  });

  it('redirects an unauthenticated visitor away from /notificaciones', async () => {
    renderApp('/notificaciones');

    expect(await screen.findByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', { name: /^Notificaciones$/i }),
    ).not.toBeInTheDocument();
  });

  it('redirects a TD user away from the CC-only notifications route', async () => {
    seedSession('TD');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({ userId: 'usr-td', grantedPermissions: [] }),
      ),
    );

    renderApp('/notificaciones');

    await waitFor(() => {
      expect(
        screen.queryByRole('heading', { name: /^Notificaciones$/i }),
      ).not.toBeInTheDocument();
    });
  });

  it('exposes the notifications entry in the sidebar for a CC user', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json(coordinatorSummary()),
      ),
    );

    renderApp('/dashboard');

    const link = await screen.findByRole('link', { name: /notificaciones/i });
    expect(link).toHaveAttribute('href', '/notificaciones');
  });

  it('shows the notification bell in the dashboard header for a CC user', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json(coordinatorSummary()),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 3 }),
      ),
    );

    renderApp('/dashboard');

    const bell = await screen.findByRole('button', { name: /notificaciones/i });
    expect(bell).toBeInTheDocument();
    expect(await screen.findByTestId('unread-badge')).toHaveTextContent('3');
  });

  it('renders the coordinator alerts panel inside the CC dashboard section', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json(coordinatorSummary()),
      ),
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderApp('/dashboard');

    expect(
      await screen.findByText(/alertas y notificaciones activas/i),
    ).toBeInTheDocument();
    expect(
      await screen.findByText('Indicador 1.2 con observaciones'),
    ).toBeInTheDocument();
  });

  it('does not render the editable alerts panel for a read-only EE view', async () => {
    seedSession('EE');
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({
          userId: 'usr-ee',
          grantedPermissions: ['READ_EE_DASHBOARD'],
          coordinatorSection: {
            programId: 'prog-otros',
            programName: 'Ingeniería Civil',
            totalIndicators: 20,
            overallProgressPercentage: 40,
            approvedEvidences: 30,
            rejectedEvidences: 2,
            pendingObservations: 1,
            phaseProgressList: [],
            bottlenecks: [],
          },
        }),
      ),
    );

    renderApp('/dashboard');

    expect(await screen.findByText(/Revisión documental — Ingeniería Civil/i)).toBeInTheDocument();
    expect(
      screen.queryByText(/alertas y notificaciones activas/i),
    ).not.toBeInTheDocument();
  });
});
