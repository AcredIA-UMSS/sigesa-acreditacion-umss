import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { useDashboardSummary } from './dashboardHooks';
import { server } from '../../../test/mocks/server';

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('useDashboardSummary', () => {
  it('shouldMapSuccessfulSummary', async () => {
    const { result } = renderHook(() => useDashboardSummary(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.summary?.coordinatorSection?.programName).toBe('Ingeniería de Sistemas');
    expect(result.current.summary?.coordinatorSection?.totalIndicadores).toBe(48);
  });

  it('shouldExposeHttpErrors', async () => {
    server.use(
      http.get('/api/v1/dashboards/me/summary', () =>
        HttpResponse.json({ error: 'FORBIDDEN', message: 'denied' }, { status: 403 }),
      ),
    );
    const { result } = renderHook(() => useDashboardSummary(), { wrapper });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
