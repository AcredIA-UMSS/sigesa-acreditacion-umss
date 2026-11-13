import { act, renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../../../lib/auth/AuthProvider';
import { seedSession } from '../../../test/session';
import { createTestQueryClient } from '../../../test/test-utils';
import { server } from '../../../test/mocks/server';
import { useNotificationBell } from './useNotificationBell';

const navigateMock = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

function createWrapper() {
  const queryClient = createTestQueryClient();
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter initialEntries={['/dashboard']}>{children}</MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );
  };
}

describe('useNotificationBell', () => {
  beforeEach(() => {
    navigateMock.mockReset();
    seedSession('JD');
  });

  it('marks notification read and navigates to deepLink on item click', async () => {
    let markReadCalled = false;
    server.use(
      http.get('/api/v1/notifications/me', () =>
        HttpResponse.json({
          content: [
            {
              id: '11111111-1111-1111-1111-111111111111',
              eventType: 'ProcessClosed',
              title: 'Proceso cerrado',
              body: 'Revise el semáforo',
              deepLink: '/executive/semaphore',
              processId: '22222222-2222-2222-2222-222222222222',
              readAt: null,
              createdAt: '2026-11-12T12:00:00',
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 15,
          number: 0,
        }),
      ),
      http.post('/api/v1/notifications/:id/read', () => {
        markReadCalled = true;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    const { result } = renderHook(() => useNotificationBell(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.items).toHaveLength(1);
    });

    await act(async () => {
      await result.current.onItemClick(result.current.items[0]);
    });

    expect(markReadCalled).toBe(true);
    expect(navigateMock).toHaveBeenCalledWith('/executive/semaphore');
  });
});
