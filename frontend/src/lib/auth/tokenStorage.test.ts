import { describe, expect, it } from 'vitest';
import { AUTH_STORAGE_KEY } from './types';
import { clearSession, loadSession, saveSession } from './tokenStorage';
import { buildSession } from '../../test/session';

describe('tokenStorage', () => {
  it('shouldReturnNullWhenTokenIsAbsent', () => {
    expect(loadSession()).toBeNull();
  });

  it('shouldPersistAndLoadAnActiveSession', () => {
    const session = buildSession();
    saveSession(session);
    expect(loadSession()).toEqual(session);
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toContain(session.accessToken);
  });

  it('shouldClearExpiredSession', () => {
    saveSession(buildSession({ expiresAt: Date.now() - 1_000 }));
    expect(loadSession()).toBeNull();
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull();
  });

  it('shouldClearCorruptedSessionPayload', () => {
    localStorage.setItem(AUTH_STORAGE_KEY, '{not-json');
    expect(loadSession()).toBeNull();
  });

  it('shouldRemoveSessionOnLogoutStorageClear', () => {
    saveSession(buildSession());
    clearSession();
    expect(loadSession()).toBeNull();
  });
});
