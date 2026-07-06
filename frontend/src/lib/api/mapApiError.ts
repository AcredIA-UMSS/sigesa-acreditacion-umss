import { isApiError } from './apiError';

/** Maps known API error codes to UI messages (backend `message` is source of truth). */
export function getApiErrorMessage(error: unknown, fallback = 'Ocurrió un error inesperado.'): string {
  if (isApiError(error)) {
    return error.message;
  }

  if (error instanceof Error && error.message.length > 0) {
    return error.message;
  }

  return fallback;
}

/** Login always shows a generic message on 401 to avoid account enumeration (UC-001 A1). */
export function getLoginErrorMessage(error: unknown): string {
  if (isApiError(error) && error.code === 'AUTH_INVALID_CREDENTIALS') {
    return 'Credenciales inválidas';
  }

  if (isApiError(error) && error.code === 'ACCESS_DENIED') {
    return error.message;
  }

  if (isApiError(error) && error.code === 'NETWORK_ERROR') {
    return error.message;
  }

  if (isApiError(error) && (error.status === 502 || error.status === 503 || error.status === 504)) {
    return error.message;
  }

  return getApiErrorMessage(error, 'No fue posible iniciar sesión. Intente nuevamente.');
}
