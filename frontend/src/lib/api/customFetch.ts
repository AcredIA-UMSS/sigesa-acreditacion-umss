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
): Promise<TData> {
  const { auth = true, headers: initHeaders, ...init } = options;
  const headers = new Headers(initHeaders);

  if (auth) {
    const session = loadSession();
    if (session?.accessToken) {
      headers.set('Authorization', `Bearer ${session.accessToken}`);
    }
  }

  // FormData must set its own multipart boundary; never force JSON.
  if (
    init.body !== undefined
    && !(init.body instanceof FormData)
    && !headers.has('Content-Type')
  ) {
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

  if (!response.ok) {
    const rawBody = isEmptyBody ? null : await response.text();
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

  if (isEmptyBody) {
    return { data: undefined, status: response.status, headers: response.headers } as unknown as TData;
  }

  const contentType = response.headers.get('Content-Type') || '';
  const isBinary = contentType.includes('application/pdf') ||
                   contentType.includes('spreadsheetml') ||
                   contentType.includes('octet-stream');

  let data: any;
  if (isBinary) {
    data = await response.blob();
  } else {
    const rawBody = await response.text();
    data = rawBody && rawBody.length > 0 ? JSON.parse(rawBody) : undefined;
  }

  return { data, status: response.status, headers: response.headers } as unknown as TData;
}
