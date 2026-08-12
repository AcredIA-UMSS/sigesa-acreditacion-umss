import { useCallback, useLayoutEffect, useMemo, useState, type ReactNode } from 'react';
import type { LoginResponse } from '../../api/model';
import { registerAuthBridge } from './authBridge';
import { AuthContext, type AuthContextValue } from './auth-context';
import { isBackendRoleCode } from './roleLabels';
import { clearSession, loadSession, saveSession } from './tokenStorage';
import type { AuthSession } from './types';

function toSession(response: LoginResponse): AuthSession | null {
  const accessToken = response.accessToken;
  const role = response.role;
  const expiresIn = response.expiresIn ?? 0;

  if (!accessToken || !role || !isBackendRoleCode(role)) {
    return null;
  }

  return {
    accessToken,
    expiresIn,
    role,
    programScope: response.programScope ?? [],
    expiresAt: Date.now() + expiresIn * 1000,
  };
}

function isSessionActive(session: AuthSession | null): session is AuthSession {
  return session !== null && session.expiresAt > Date.now();
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => {
    const stored = loadSession();
    return isSessionActive(stored) ? stored : null;
  });

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  useLayoutEffect(() => {
    return registerAuthBridge(
      () => (isSessionActive(session) ? session.accessToken : null),
      logout,
    );
  }, [session, logout]);

  const login = useCallback((response: LoginResponse) => {
    const nextSession = toSession(response);
    if (!nextSession) {
      throw new Error('Respuesta de autenticación inválida.');
    }
    saveSession(nextSession);
    setSession(nextSession);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: isSessionActive(session),
      login,
      logout,
    }),
    [session, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
