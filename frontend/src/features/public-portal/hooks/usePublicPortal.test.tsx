import { act, renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';
import { usePublicPortal } from './usePublicPortal';

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('usePublicPortal', () => {
  it('filters published programs by name on the client', async () => {
    const { result } = renderHook(() => usePublicPortal(), { wrapper });

    await waitFor(() => {
      expect(result.current.filteredPrograms).toHaveLength(2);
    });

    act(() => {
      result.current.setSearchQuery('sistemas');
    });

    expect(result.current.filteredPrograms).toEqual([
      { slug: 'inf-sis', name: 'Ingeniería de Sistemas' },
    ]);
    expect(result.current.filteredPrograms.some((item) => item.slug === 'ing-civ')).toBe(false);
  });
});
