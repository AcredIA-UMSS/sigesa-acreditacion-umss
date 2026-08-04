import { resolveAccessToken } from '../auth/authBridge';
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

  if (status === 401) {
    return 'Sesión expirada o no autenticada. Inicie sesión nuevamente.';
  }

  return 'Error inesperado';
}

export async function customFetch<TData>(
  url: string,
  options: CustomFetchOptions = {},
): Promise<TData> {
  const { auth = true, headers: initHeaders, ...init } = options;
  const headers = new Headers(initHeaders);


  // Vacío = rutas relativas (/api/...) vía proxy Vite o nginx en Docker
  const baseUrl = process.env.VITE_API_URL ?? '';
  const fullUrl = url.startsWith('http') ? url : `${baseUrl}${url}`;
  const shouldAttachAuth = auth && !fullUrl.includes('/auth/login');

  let authorizationAttached = false;
  if (shouldAttachAuth) {
    const accessToken = resolveAccessToken();
    if (accessToken) {
      headers.set('Authorization', `Bearer ${accessToken}`);
      authorizationAttached = true;
    }
  }

  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response: Response;

  try {
    response = await fetch(fullUrl, { ...init, headers });
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

    if (response.status === 401 && authorizationAttached && code === 'UNAUTHORIZED') {
      // No cerrar sesión automáticamente: evita redirección al login en errores transitorios.
      // La UI muestra el error; el usuario puede reintentar o cerrar sesión manualmente.
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
