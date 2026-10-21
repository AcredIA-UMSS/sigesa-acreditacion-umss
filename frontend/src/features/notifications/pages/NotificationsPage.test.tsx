import { http, HttpResponse } from 'msw';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { NotificationsPage } from './NotificationsPage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';
import {
  buildNotificationsPage,
  notificationFixtureDeadline,
  notificationFixtureRejected,
} from '../../../test/mocks/handlers';

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderPage() {
  return renderWithProviders(
    <>
      <NotificationsPage />
      <LocationProbe />
    </>,
    { route: '/notificaciones' },
  );
}

describe('NotificationsPage', () => {
  it('renders the notifications list with unread indicator and unread count chip', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected, notificationFixtureDeadline]),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    expect(
      await screen.findByText('Indicador 1.2 con observaciones'),
    ).toBeInTheDocument();
    expect(screen.getByText('Plazo próximo para Nivel 1')).toBeInTheDocument();
    expect(screen.getByText('1 sin leer')).toBeInTheDocument();
    expect(screen.getByTestId('unread-dot-notif-rejected-1')).toBeInTheDocument();
    expect(screen.queryByTestId('unread-dot-notif-deadline-1')).not.toBeInTheDocument();
  });

  it('marks a notification as read and follows its deep link on click', async () => {
    seedSession('CC');
    const markedRead: string[] = [];
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
      http.patch('/api/v1/notifications/:id/read', ({ params }) => {
        markedRead.push(String(params.id));
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderPage();

    fireEvent.click(await screen.findByText('Indicador 1.2 con observaciones'));

    await waitFor(() => {
      expect(markedRead).toEqual(['notif-rejected-1']);
    });
    expect(screen.getByTestId('location')).toHaveTextContent(
      '/procesos/p-1?indicadorId=i-1',
    );
  });

  it('marks all notifications as read via the header action', async () => {
    seedSession('CC');
    let markAllCalled = 0;
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
      http.post('/api/v1/notifications/mark-all-read', () => {
        markAllCalled += 1;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: /marcar todas como leídas/i }));

    await waitFor(() => {
      expect(markAllCalled).toBe(1);
    });
  });

  it('requests only unread notifications when the filter is toggled', async () => {
    seedSession('CC');
    const requestedUrls: string[] = [];
    server.use(
      http.get('/api/v1/notifications/me', ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], { totalElements: 1 }),
        );
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: /ver solo no leídas/i }));

    await waitFor(() => {
      expect(
        requestedUrls.some((url) => url.includes('unreadOnly=true')),
      ).toBe(true);
    });
    expect(screen.getByRole('button', { name: /solo no leídas/i })).toBeInTheDocument();
  });

  it('shows pagination controls and requests the next page', async () => {
    seedSession('CC');
    const requestedUrls: string[] = [];
    server.use(
      http.get('/api/v1/notifications/me', ({ request }) => {
        requestedUrls.push(request.url);
        return HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 40,
            totalPages: 2,
            size: 20,
          }),
        );
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    expect(await screen.findByText(/página 1 de 2/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /siguiente/i }));

    await waitFor(() => {
      expect(requestedUrls.some((url) => url.includes('page=1'))).toBe(true);
    });
  });

  it('shows an error state with retry when the request fails', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          { error: 'INTERNAL_ERROR', message: 'Fallo interno' },
          { status: 500 },
        ),
      ),
    );

    renderPage();

    expect(
      await screen.findByRole('heading', { name: /error al cargar notificaciones/i }),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument();
  });

  it('shows the empty state when there are no notifications', async () => {
    seedSession('CC');

    renderPage();

    expect(
      await screen.findByText(/no tiene notificaciones registradas/i),
    ).toBeInTheDocument();
  });

  it('shows the unread-specific empty copy while the unread filter is active', async () => {
    seedSession('CC');

    renderPage();

    await screen.findByText(/no tiene notificaciones registradas/i);

    fireEvent.click(screen.getByRole('button', { name: /ver solo no leídas/i }));

    expect(
      await screen.findByText(/no tiene notificaciones sin leer/i),
    ).toBeInTheDocument();
  });

  it('does not navigate or mark as read when an already-read notification has no deep link', async () => {
    seedSession('CC');
    let patchCalls = 0;
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([{ ...notificationFixtureDeadline, deepLink: null }])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 0 }),
      ),
      http.patch('/api/v1/notifications/:id/read', () => {
        patchCalls += 1;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderPage();

    fireEvent.click(await screen.findByText('Plazo próximo para Nivel 1'));

    expect(patchCalls).toBe(0);
    expect(screen.getByTestId('location')).toHaveTextContent('/notificaciones');
  });

  it('renders the creation timestamp in Spanish locale format', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    const expected = new Date(notificationFixtureRejected.createdAt).toLocaleString('es-BO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
    expect(await screen.findByText(expected)).toBeInTheDocument();
  });

  it('hides the unread chip and mark-all action when everything is read', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureDeadline])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 0 }),
      ),
    );

    renderPage();

    await screen.findByText('Plazo próximo para Nivel 1');

    expect(screen.queryByText(/sin leer/i)).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: /marcar todas como leídas/i }),
    ).not.toBeInTheDocument();
  });

  it('disables the previous button on the first page and the next button on the last', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 40,
            totalPages: 2,
            size: 20,
          }),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    await screen.findByText(/página 1 de 2/i);
    expect(screen.getByRole('button', { name: /anterior/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /siguiente/i })).toBeEnabled();
  });

  it('recovers from an error state after a successful retry', async () => {
    seedSession('CC');
    let attempt = 0;
    server.use(
      http.get('/api/v1/notifications/me', () => {
        attempt += 1;
        if (attempt === 1) {
          return HttpResponse.json(
            { error: 'INTERNAL_ERROR', message: 'Fallo interno' },
            { status: 500 },
          );
        }
        return HttpResponse.json(buildNotificationsPage([notificationFixtureRejected]));
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: /reintentar/i }));

    expect(
      await screen.findByText('Indicador 1.2 con observaciones'),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', { name: /error al cargar notificaciones/i }),
    ).not.toBeInTheDocument();
  });

  it('toggles the unread filter accessibility state', async () => {
    seedSession('CC');

    renderPage();

    const toggle = await screen.findByRole('button', { name: /ver solo no leídas/i });
    expect(toggle).toHaveAttribute('aria-pressed', 'false');

    fireEvent.click(toggle);

    expect(
      screen.getByRole('button', { name: /^solo no leídas$/i }),
    ).toHaveAttribute('aria-pressed', 'true');
  });

  it('renders the event-specific icon per notification type', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([
            notificationFixtureRejected,
            { ...notificationFixtureDeadline, id: 'n-approved', eventType: 'IndicatorApproved' },
          ]),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    const { container } = renderPage();

    await screen.findByText('Indicador 1.2 con observaciones');

    // lucide-react renders its icons as <svg class="lucide lucide-<name>">;
    // assert on the stroke colour classes our event mapping applies instead,
    // which is the behaviour we actually own.
    expect(container.querySelectorAll('svg.text-secondary').length).toBeGreaterThan(0);
    expect(container.querySelectorAll('svg.text-success').length).toBeGreaterThan(0);
  });

  it('falls back to the neutral info icon for an unknown event type', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([
            { ...notificationFixtureRejected, id: 'n-unknown', eventType: 'SomethingElse' },
          ]),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    const { container } = renderPage();

    await screen.findByText('Indicador 1.2 con observaciones');

    // Scope to the list markup: the page header also renders an icon.
    const list = container.querySelector('ul');
    expect(list?.querySelectorAll('svg.text-primary-500').length).toBe(1);
    expect(list?.querySelectorAll('svg.text-secondary').length).toBe(0);
  });

  it('shows the skeleton placeholder while the first page is loading', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', async () => {
        await new Promise((resolve) => setTimeout(resolve, 50));
        return HttpResponse.json(buildNotificationsPage([notificationFixtureRejected]));
      }),
    );

    const { container } = renderPage();

    expect(container.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);

    await screen.findByText('Indicador 1.2 con observaciones');
  });

  it('disables the next button once the last page is reached', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 40,
            totalPages: 2,
            size: 20,
          }),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    await screen.findByText(/página 1 de 2/i);
    fireEvent.click(screen.getByRole('button', { name: /siguiente/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /siguiente/i })).toBeDisabled();
    });
    expect(screen.getByRole('button', { name: /anterior/i })).toBeEnabled();
  });

  it('hides pagination controls when everything fits on one page', async () => {
    seedSession('CC');
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    await screen.findByText('Indicador 1.2 con observaciones');

    expect(screen.queryByRole('button', { name: /siguiente/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /anterior/i })).not.toBeInTheDocument();
  });

  it('navigates back to the previous page', async () => {
    seedSession('CC');
    const requestedPages: string[] = [];
    server.use(
      http.get('/api/v1/notifications/me', ({ request }) => {
        requestedPages.push(new URL(request.url).searchParams.get('page') ?? '');
        return HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 40,
            totalPages: 2,
            size: 20,
          }),
        );
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
    );

    renderPage();

    await screen.findByText(/página 1 de 2/i);
    fireEvent.click(screen.getByRole('button', { name: /siguiente/i }));
    await waitFor(() => expect(screen.getByText(/página 2 de 2/i)).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: /anterior/i }));

    await waitFor(() => expect(screen.getByText(/página 1 de 2/i)).toBeInTheDocument());
    expect(requestedPages.filter((p) => p === '1')).toHaveLength(1);
  });
});
