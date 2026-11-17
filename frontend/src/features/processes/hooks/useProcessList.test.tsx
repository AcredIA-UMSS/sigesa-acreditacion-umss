import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { useProcessList } from './useProcessList';
import { server } from '../../../test/mocks/server';

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('useProcessList', () => {
  it('shouldReturnProcessesOnSuccess', async () => {
    const { result } = renderHook(() => useProcessList(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.processes[0]?.careerName).toBe('Ingeniería de Sistemas');
    expect(result.current.isError).toBe(false);
  });

  it('shouldReturnEmptyListWhenApiReturnsEmptyArray', async () => {
    server.use(http.get('/api/v1/processes', () => HttpResponse.json([])));
    const { result } = renderHook(() => useProcessList(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.processes).toEqual([]);
  });
});
