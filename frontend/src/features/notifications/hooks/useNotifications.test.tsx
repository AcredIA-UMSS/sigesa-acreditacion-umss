import { http, HttpResponse } from 'msw';
import { act, renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';
import {
  useNotifications,
  NOTIFICATIONS_QUERY_KEY,
  UNREAD_COUNT_QUERY_KEY,
} from './useNotifications';
import { server } from '../../../test/mocks/server';
import {
  buildNotificationsPage,
  notificationFixtureRejected,
} from '../../../test/mocks/handlers';
import type { NotificationQueryParams } from '../api/notificationsApi';

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return {
    queryClient,
    wrapper: ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  };
}

describe('useNotifications', () => {
  it('exposes the notifications page and the unread counter', async () => {
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 42,
            totalPages: 3,
          }),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 9 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications({ size: 20 }), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.notifications).toHaveLength(1);
    expect(result.current.totalElements).toBe(42);
    expect(result.current.totalPages).toBe(3);
    expect(result.current.unreadCount).toBe(9);
    expect(result.current.page).toBe(0);
  });

  it('requests the selected page and updates the page state', async () => {
    const requestedPages: string[] = [];
    server.use(
      http.get('/api/v1/notifications/me', ({ request }) => {
        requestedPages.push(new URL(request.url).searchParams.get('page') ?? '');
        return HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 60,
            totalPages: 3,
          }),
        );
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 0 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications({ size: 20 }), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.setPage(2));

    await waitFor(() => expect(result.current.page).toBe(2));
    await waitFor(() => expect(requestedPages).toContain('2'));
  });

  it('derives totalPages from totalElements when the backend omits it', async () => {
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json({
          content: [notificationFixtureRejected],
          totalElements: 45,
          size: 20,
          number: 0,
        }),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 0 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications({ size: 20 }), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.totalPages).toBe(3);
  });

  it('never reports fewer than one page when there are no notifications', async () => {
    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.totalElements).toBe(0);
    expect(result.current.totalPages).toBe(1);
  });

  it('resets to page 0 when the unreadOnly filter changes', async () => {
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(
          buildNotificationsPage([notificationFixtureRejected], {
            totalElements: 60,
            totalPages: 3,
          }),
        ),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 0 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result, rerender } = renderHook(
      ({ params }: { params: NotificationQueryParams }) => useNotifications(params),
      { wrapper, initialProps: { params: { size: 20 } as NotificationQueryParams } },
    );

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.setPage(2));
    await waitFor(() => expect(result.current.page).toBe(2));

    rerender({ params: { size: 20, unreadOnly: true } });

    await waitFor(() => expect(result.current.page).toBe(0));
  });

  it('marks a single notification as read and refreshes both queries', async () => {
    let markReadCalls = 0;
    let listRequests = 0;
    server.use(
      http.get('/api/v1/notifications/me', () => {
        listRequests += 1;
        return HttpResponse.json(buildNotificationsPage([notificationFixtureRejected]));
      }),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
      http.patch('/api/v1/notifications/:id/read', () => {
        markReadCalls += 1;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { queryClient, wrapper } = createWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const { result } = renderHook(() => useNotifications(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    const requestsBefore = listRequests;

    act(() => result.current.markAsRead('notif-rejected-1'));

    await waitFor(() => expect(markReadCalls).toBe(1));
    await waitFor(() =>
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: NOTIFICATIONS_QUERY_KEY }),
    );
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: UNREAD_COUNT_QUERY_KEY });
    await waitFor(() => expect(listRequests).toBeGreaterThan(requestsBefore));
  });

  it('marks all notifications as read and toggles the pending flag', async () => {
    let markAllCalls = 0;
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json(buildNotificationsPage([notificationFixtureRejected])),
      ),
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ unreadCount: 1 }),
      ),
      http.post('/api/v1/notifications/mark-all-read', () => {
        markAllCalls += 1;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { queryClient, wrapper } = createWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const { result } = renderHook(() => useNotifications(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.markAllAsRead());

    await waitFor(() => expect(markAllCalls).toBe(1));
    await waitFor(() =>
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: UNREAD_COUNT_QUERY_KEY }),
    );
    await waitFor(() => expect(result.current.isMarkingAll).toBe(false));
  });

  it('surfaces an error state when the notifications request fails', async () => {
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json({ error: 'INTERNAL_ERROR', message: 'Fallo' }, { status: 500 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications(), { wrapper });

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.notifications).toEqual([]);
  });

  it('keeps the unread counter at zero when its request fails', async () => {
    server.use(
      http.get('/api/v1/notifications/unread-count', () =>
        HttpResponse.json({ error: 'INTERNAL_ERROR', message: 'Fallo' }, { status: 500 }),
      ),
    );

    const { wrapper } = createWrapper();
    const { result } = renderHook(() => useNotifications(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.unreadCount).toBe(0);
  });
});
