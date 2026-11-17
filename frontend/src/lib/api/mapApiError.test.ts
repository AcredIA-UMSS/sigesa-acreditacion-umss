import { describe, expect, it } from 'vitest';
import { ApiError } from './apiError';
import { getApiErrorMessage, getLoginErrorMessage } from './mapApiError';

describe('mapApiError', () => {
  it('shouldMapGenericApiErrors', () => {
    expect(getApiErrorMessage(new ApiError(500, 'X', 'Fallo interno'))).toBe('Fallo interno');
    expect(getApiErrorMessage(new Error('boom'))).toBe('boom');
    expect(getApiErrorMessage('nope')).toBe('Ocurrió un error inesperado.');
  });

  it('shouldHideAccountEnumerationOnInvalidCredentials', () => {
    expect(getLoginErrorMessage(new ApiError(401, 'AUTH_INVALID_CREDENTIALS', 'detalle'))).toBe(
      'Credenciales inválidas',
    );
  });

  it('shouldSurfaceAccessDeniedAndNetworkErrors', () => {
    expect(getLoginErrorMessage(new ApiError(403, 'ACCESS_DENIED', 'Sin rol'))).toBe('Sin rol');
    expect(getLoginErrorMessage(new ApiError(0, 'NETWORK_ERROR', 'Sin conexión'))).toBe('Sin conexión');
  });
});
