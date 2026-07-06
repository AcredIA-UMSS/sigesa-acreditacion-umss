import { loadSession } from '../auth/tokenStorage';
import { ApiError } from './apiError';

export interface CustomFetchOptions extends RequestInit {
  /** When false, Authorization header is omitted (e.g. login). Default: true. */
  auth?: boolean;
}

function resolveHttpErrorMessage(status: number, rawBody: string | null): string {
  if (rawBody) {
    try {
      const parsed = JSON.parse(rawBody) as { error?: string; message?: string };
      if (parsed.message) {
        return parsed.message;
      }
    } catch {
      if (rawBody.length < 200) {
        return rawBody;
      }
    }
  }

  if (status === 502 || status === 503 || status === 504) {
    return 'No se pudo conectar con el servidor. Verifique que el backend esté ejecutándose en http://localhost:8080.';
  }

  if (status === 404) {
    return 'El endpoint solicitado no existe. Verifique que el backend esté actualizado.';
  }

  return 'Error inesperado';
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

  let response: Response;

  try {
    response = await fetch(url, { ...init, headers });
  } catch {
    throw new ApiError(
      0,
      'NETWORK_ERROR',
      'No se pudo conectar con el servidor. Verifique que el backend esté ejecutándose en http://localhost:8080.',
    );
  }

  const isEmptyBody = [204, 205, 304].includes(response.status);
  const rawBody = isEmptyBody ? null : await response.text();

  if (!response.ok) {
    let code = 'UNKNOWN_ERROR';

    if (rawBody) {
      try {
        const parsed = JSON.parse(rawBody) as { error?: string; message?: string };
        code = parsed.error ?? code;
      } catch {
        /* keep UNKNOWN_ERROR */
      }
    }

    throw new ApiError(response.status, code, resolveHttpErrorMessage(response.status, rawBody));
  }

  const data = rawBody && rawBody.length > 0 ? (JSON.parse(rawBody) as TData) : (undefined as TData);

  return { data, status: response.status, headers: response.headers };
}
