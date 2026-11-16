import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';
import { useHomeNews } from './useHomeNews';

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('useHomeNews', () => {
  it('excludes the principal slug from the side excerpt', async () => {
    const { result } = renderHook(() => useHomeNews(), { wrapper });

    await waitFor(() => {
      expect(result.current.featured?.slug).toBe(
        'facultad-de-medicina-renueva-acreditacion-mercosur',
      );
    });

    expect(result.current.excerpt.map((item) => item.slug)).toEqual([
      'taller-de-autoevaluacion-para-nuevas-carreras',
    ]);
  });
});
