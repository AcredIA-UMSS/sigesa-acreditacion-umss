import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { LoginResponse } from '../../api/model';
import { isBackendRoleCode } from './roleLabels';
import { clearSession, loadSession, saveSession } from './tokenStorage';
import type { AuthSession } from './types';

interface AuthContextValue {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (response: LoginResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => loadSession());

  const login = useCallback((response: LoginResponse) => {
    const nextSession = toSession(response);
    if (!nextSession) {
      throw new Error('Respuesta de autenticación inválida.');
    }
    saveSession(nextSession);
    setSession(nextSession);
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: session !== null,
      login,
      logout,
    }),
    [session, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider.');
  }
  return context;
}

export function getPostLoginPath(role: string): string {
  if (role === 'JD') {
    return '/admin/users';
  }
  return '/procesos/nuevo';
}
