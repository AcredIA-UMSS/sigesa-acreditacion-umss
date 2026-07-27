import { isAxiosError } from 'axios';

const ERROR_LABELS: Record<string, string> = {
  EVIDENCE_UNCLASSIFIED: 'Complete el indicador y el criterio antes de cargar.',
  INVALID_EVIDENCE_FORMAT: 'Formato de archivo no permitido.',
  INDICATOR_NOT_FOUND: 'Indicador no encontrado.',
  INDICATOR_NOT_UPLOADABLE: 'El indicador no admite carga en su estado actual.',
  PROGRAM_SCOPE_DENIED: 'No tiene permiso sobre la carrera de este indicador.',
  UPLOAD_IN_PROGRESS: 'Ya hay una carga en curso para este indicador.',
  PAYLOAD_TOO_LARGE: 'El archivo supera el tamaño máximo permitido (50 MB).',
};

export function mapUploadError(error: Error | null): string | null {
  if (!error) return null;

  if (isAxiosError<{ error?: string; message?: string }>(error)) {
    const code = error.response?.data?.error;
    const message = error.response?.data?.message;
    if (code && ERROR_LABELS[code]) return ERROR_LABELS[code];
    if (message) return message;
  }

  return error.message || 'Error al cargar la evidencia.';
}
