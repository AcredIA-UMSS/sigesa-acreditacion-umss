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
  const fromBridge = accessTokenGetter();
  if (fromBridge) {
    return fromBridge;
  }

  const stored = loadSession();
  return stored?.accessToken ?? null;
}

export function notifyUnauthorized(): void {
  unauthorizedHandler?.();
}
