import { isApiError } from '../../../lib/api/apiError';

const ERROR_LABELS: Record<string, string> = {
  ASSISTANT_UNAVAILABLE:
    'El asistente no está configurado o la API key de Open WebUI es inválida. Actualice SIGESA_ASSISTANT_API_KEY en .env y reinicie el backend.',
  ASSISTANT_COMPLETION_FAILED:
    'No se pudo obtener respuesta del modelo. Verifique que Open WebUI esté activo.',
};

export function mapAssistantError(error: Error | null): string | null {
  if (!error) return null;

  if (isApiError(error)) {
    if (error.message && error.message !== ERROR_LABELS[error.code]) {
      return error.message;
    }
    if (ERROR_LABELS[error.code]) return ERROR_LABELS[error.code];
    return error.message;
  }

  return error.message || 'Error al contactar al asistente.';
}
