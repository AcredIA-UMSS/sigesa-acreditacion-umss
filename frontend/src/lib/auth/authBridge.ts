import { loadSession } from './tokenStorage';

type AccessTokenGetter = () => string | null;
type UnauthorizedHandler = () => void;

let accessTokenGetter: AccessTokenGetter = () => null;
let unauthorizedHandler: UnauthorizedHandler | null = null;

export function registerAuthBridge(
  getAccessToken: AccessTokenGetter,
  onUnauthorized: UnauthorizedHandler,
): () => void {
  accessTokenGetter = getAccessToken;
  unauthorizedHandler = onUnauthorized;

  return () => {
    accessTokenGetter = () => null;
    unauthorizedHandler = null;
  };
}

export function resolveAccessToken(): string | null {
  // Prefer localStorage: saveSession() runs before React re-renders the auth bridge.
  const stored = loadSession();
  if (stored?.accessToken) {
    return stored.accessToken;
  }

  return accessTokenGetter();
}

export function notifyUnauthorized(): void {
  unauthorizedHandler?.();
}
