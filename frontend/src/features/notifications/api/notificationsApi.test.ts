import { http, HttpResponse } from 'msw';
import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  fetchMyNotifications,
  fetchUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
} from './notificationsApi';
import { server } from '../../../test/mocks/server';
import {
  buildNotificationsPage,
  notificationFixtureRejected,
} from '../../../test/mocks/handlers';

describe('notificationsApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('fetchMyNotifications', () => {
    it('sends unreadOnly, page and size as query params and returns the page payload', async () => {
      let capturedUrl = '';
      server.use(
        http.get('/api/v1/notifications/me', ({ request }) => {
          capturedUrl = request.url;
          return HttpResponse.json(
            buildNotificationsPage([notificationFixtureRejected], {
              totalElements: 42,
              totalPages: 3,
            }),
          );
        }),
      );

      const result = await fetchMyNotifications({ unreadOnly: true, page: 2, size: 5 });

      const url = new URL(capturedUrl);
      expect(url.searchParams.get('unreadOnly')).toBe('true');
      expect(url.searchParams.get('page')).toBe('2');
      expect(url.searchParams.get('size')).toBe('5');
      expect(result.totalElements).toBe(42);
      expect(result.content).toHaveLength(1);
    });

    it('defaults page to 0 and size to 20 when no params are given', async () => {
      let capturedUrl = '';
      server.use(
        http.get('/api/v1/notifications/me', ({ request }) => {
          capturedUrl = request.url;
          return HttpResponse.json(buildNotificationsPage([]));
        }),
      );

      await fetchMyNotifications();

      const url = new URL(capturedUrl);
      expect(url.searchParams.get('page')).toBe('0');
      expect(url.searchParams.get('size')).toBe('20');
      expect(url.searchParams.has('unreadOnly')).toBe(false);
    });

    it('propagates an ApiError when the backend responds with a failure', async () => {
      server.use(
        http.get('/api/v1/notifications/me', () =>
          HttpResponse.json(
            { error: 'INTERNAL_ERROR', message: 'Fallo interno' },
            { status: 500 },
          ),
        ),
      );

      await expect(fetchMyNotifications()).rejects.toMatchObject({ status: 500 });
    });
  });

  describe('fetchUnreadCount', () => {
    it('returns the unread counter value', async () => {
      server.use(
        http.get('/api/v1/notifications/unread-count', () =>
          HttpResponse.json({ unreadCount: 7 }),
        ),
      );

      await expect(fetchUnreadCount()).resolves.toBe(7);
    });

    it('propagates an ApiError when the backend responds with a failure', async () => {
      server.use(
        http.get('/api/v1/notifications/unread-count', () =>
          HttpResponse.json({ error: 'FORBIDDEN', message: 'Sin permisos' }, { status: 403 }),
        ),
      );

      await expect(fetchUnreadCount()).rejects.toMatchObject({ status: 403 });
    });
  });

  describe('markNotificationAsRead', () => {
    it('PATCHes the notification read endpoint and resolves on 204', async () => {
      let capturedUrl = '';
      let capturedMethod = '';
      server.use(
        http.patch('/api/v1/notifications/:id/read', ({ request, params }) => {
          capturedUrl = request.url;
          capturedMethod = request.method;
          expect(params.id).toBe('notif-rejected-1');
          return new HttpResponse(null, { status: 204 });
        }),
      );

      await expect(markNotificationAsRead('notif-rejected-1')).resolves.toBeUndefined();
      expect(capturedMethod).toBe('PATCH');
      expect(capturedUrl).toContain('/api/v1/notifications/notif-rejected-1/read');
    });

    it('propagates an ApiError when the notification does not exist', async () => {
      server.use(
        http.patch('/api/v1/notifications/:id/read', () =>
          HttpResponse.json({ error: 'NOT_FOUND', message: 'No existe' }, { status: 404 }),
        ),
      );

      await expect(markNotificationAsRead('missing')).rejects.toMatchObject({ status: 404 });
    });
  });

  describe('markAllNotificationsAsRead', () => {
    it('POSTs to the mark-all-read endpoint and resolves on 204', async () => {
      let capturedMethod = '';
      server.use(
        http.post('/api/v1/notifications/mark-all-read', ({ request }) => {
          capturedMethod = request.method;
          return new HttpResponse(null, { status: 204 });
        }),
      );

      await expect(markAllNotificationsAsRead()).resolves.toBeUndefined();
      expect(capturedMethod).toBe('POST');
    });
  });
});
