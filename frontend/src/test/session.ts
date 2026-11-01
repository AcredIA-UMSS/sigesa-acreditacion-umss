import { AUTH_STORAGE_KEY, type AuthSession } from '../lib/auth/types';
import type { BackendRoleCode } from '../lib/auth/roleLabels';

export function buildSession(overrides: Partial<AuthSession> = {}): AuthSession {
  return {
    accessToken: 'test-access-token',
    expiresIn: 3600,
    role: 'CC',
    programScope: ['prog-sistemas-umss'],
    expiresAt: Date.now() + 3_600_000,
    ...overrides,
  };
}

export function seedSession(role: BackendRoleCode = 'CC', overrides: Partial<AuthSession> = {}): AuthSession {
  const session = buildSession({ role, ...overrides });
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
  return session;
}
