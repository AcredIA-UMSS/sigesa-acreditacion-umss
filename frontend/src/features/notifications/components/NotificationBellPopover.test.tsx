import { http, HttpResponse } from 'msw';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { NotificationBellPopover } from './NotificationBellPopover';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';

describe('NotificationBellPopover', () => {
  it('marks an unread item as read and navigates to its deep link', async () => {
    seedSession('JD');
    const user = userEvent.setup();
    let markedId: string | null = null;

    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json({
          content: [
            {
              id: 'notif-closed-1',
              title: 'Proceso cerrado',
              body: 'Ingeniería de Sistemas cerró el proceso.',
              deepLink: '/executive/semaphore',
              eventType: 'ProcessClosed',
              createdAt: '2026-11-12T10:00:00',
              readAt: null,
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 15,
          number: 0,
        }),
      ),
      http.post('/api/v1/notifications/:id/read', ({ params }) => {
        markedId = String(params.id);
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderWithProviders(
      <Routes>
        <Route path="/" element={<NotificationBellPopover />} />
        <Route path="/executive/semaphore" element={<h1>Semáforo institucional</h1>} />
      </Routes>,
      { route: '/' },
    );

    await user.click(await screen.findByRole('button', { name: /Notificaciones/ }));
    expect(await screen.findByRole('dialog', { name: 'Notificaciones recientes' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Proceso cerrado/ }));

    await waitFor(() => {
      expect(markedId).toBe('notif-closed-1');
    });
    expect(await screen.findByRole('heading', { name: 'Semáforo institucional' })).toBeInTheDocument();
  });

  it('marks a no-deep-link item as read without navigating', async () => {
    seedSession('CC');
    const user = userEvent.setup();
    let markedId: string | null = null;

    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json({
          content: [
            {
              id: 'notif-unassigned-1',
              title: 'Responsabilidad retirada',
              body: 'Ya no es responsable del proceso.',
              deepLink: '',
              eventType: 'ProcessUnassigned',
              createdAt: '2026-11-13T10:00:00',
              readAt: null,
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 15,
          number: 0,
        }),
      ),
      http.post('/api/v1/notifications/:id/read', ({ params }) => {
        markedId = String(params.id);
        return new HttpResponse(null, { status: 204 });
      }),
    );

    renderWithProviders(
      <Routes>
        <Route path="/" element={<NotificationBellPopover />} />
        <Route path="/procesos/:id" element={<h1>Detalle proceso</h1>} />
      </Routes>,
      { route: '/' },
    );

    await user.click(await screen.findByRole('button', { name: /Notificaciones/ }));
    expect(await screen.findByRole('dialog', { name: 'Notificaciones recientes' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Responsabilidad retirada/ }));

    await waitFor(() => {
      expect(markedId).toBe('notif-unassigned-1');
    });
    expect(screen.queryByRole('heading', { name: 'Detalle proceso' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Notificaciones/ })).toBeInTheDocument();
  });

});
