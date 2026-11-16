import { act, renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { usePublicPortal } from './usePublicPortal';

function wrapperFor(route: string) {
  return function Wrapper({ children }: { children: ReactNode }) {
    const client = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    return (
      <MemoryRouter initialEntries={[route]}>
        <QueryClientProvider client={client}>{children}</QueryClientProvider>
      </MemoryRouter>
    );
  };
}

describe('usePublicPortal', () => {
  it('filters published programs by name on the client', async () => {
    const { result } = renderHook(() => usePublicPortal(), { wrapper: wrapperFor('/public/status') });

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

  it('selects the career from the slug query string', async () => {
    const { result } = renderHook(() => usePublicPortal(), {
      wrapper: wrapperFor('/public/status?slug=inf-sis'),
    });

    await waitFor(() => {
      expect(result.current.selectedSlug).toBe('inf-sis');
    });
    await waitFor(() => {
      expect(result.current.status?.slug).toBe('inf-sis');
    });
  });
});
