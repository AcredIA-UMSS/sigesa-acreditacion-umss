import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { AuthProvider } from './AuthProvider';
import { useAuth } from './useAuth';
import { seedSession } from '../../test/session';
import { AUTH_STORAGE_KEY } from './types';
import type { LoginResponse } from '../../api/model';

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return (
    <QueryClientProvider client={client}>
      <AuthProvider>
        <MemoryRouter>{children}</MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

describe('AuthProvider', () => {
  it('shouldThrowWhenUseAuthIsUsedOutsideProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow('useAuth debe usarse dentro de AuthProvider.');
  });

  it('shouldRestoreActiveSessionFromStorage', () => {
    seedSession('TD');
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.session?.role).toBe('TD');
  });

  it('shouldLoginAndLogout', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.isAuthenticated).toBe(false);

    const payload: LoginResponse = {
      accessToken: 'new-token',
      expiresIn: 120,
      role: 'JD',
      programScope: [],
    };

    result.current.login(payload);
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));
    expect(result.current.session?.role).toBe('JD');

    result.current.logout();
    await waitFor(() => expect(result.current.isAuthenticated).toBe(false));
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull();
  });

  it('shouldRejectInvalidLoginPayloadWithoutToken', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(() => result.current.login({ role: 'CC', expiresIn: 10 })).toThrow(
      'Respuesta de autenticación inválida.',
    );
  });
});
