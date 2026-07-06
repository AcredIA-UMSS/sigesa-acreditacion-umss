import { loadSession } from '../auth/tokenStorage';
import { ApiError } from './apiError';

export interface CustomFetchOptions extends RequestInit {
  /** When false, Authorization header is omitted (e.g. login). Default: true. */
  auth?: boolean;
}

export async function customFetch<TData>(
  url: string,
  options: CustomFetchOptions = {},
): Promise<{ data: TData; status: number; headers: Headers }> {
  const { auth = true, headers: initHeaders, ...init } = options;
  const headers = new Headers(initHeaders);

  if (auth) {
    const session = loadSession();
    if (session?.accessToken) {
      headers.set('Authorization', `Bearer ${session.accessToken}`);
    }
  }

  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, { ...init, headers });
  const isEmptyBody = [204, 205, 304].includes(response.status);
  const rawBody = isEmptyBody ? null : await response.text();

  if (!response.ok) {
    let code = 'UNKNOWN_ERROR';
    let message = 'Error inesperado';

    if (rawBody) {
      try {
        const parsed = JSON.parse(rawBody) as { error?: string; message?: string };
        code = parsed.error ?? code;
        message = parsed.message ?? message;
      } catch {
        message = rawBody;
      }
    }

    throw new ApiError(response.status, code, message);
  }

  const data = rawBody && rawBody.length > 0 ? (JSON.parse(rawBody) as TData) : (undefined as TData);

  return { data, status: response.status, headers: response.headers };
}
