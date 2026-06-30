const ERROR_LABELS: Record<string, string> = {
  REPORT_JOB_NOT_FOUND: 'El trabajo de reporte no fue encontrado.',
  FORBIDDEN_ROLE: 'Acceso denegado. Solo el rol JD puede generar reportes ejecutivos.',
  REPORT_NOT_READY: 'El reporte aún no está listo para descargar.',
  REPORT_TEMPLATE: 'Error en la plantilla del reporte.',
  REPORT_GENERATION_FAILED: 'Falló la generación del PDF.',
};

export function mapReportError(message: string | null | undefined): string | null {
  if (!message) return null;

  for (const [code, label] of Object.entries(ERROR_LABELS)) {
    if (message.includes(code)) return label;
  }

  if (message.startsWith('Report generation failed: 403')) {
    return ERROR_LABELS.FORBIDDEN_ROLE;
  }
  if (message.startsWith('Report generation failed:')) {
    return 'No se pudo encolar la generación del reporte.';
  }
  if (message.startsWith('Status poll failed:')) {
    return 'Error al consultar el estado del reporte.';
  }
  if (message.startsWith('Download failed:')) {
    return ERROR_LABELS.REPORT_NOT_READY;
  }

  return message;
}

export function mapJobErrorCode(errorCode: string | undefined): string | null {
  if (!errorCode) return null;
  return ERROR_LABELS[errorCode] ?? errorCode;
}

export function getJobProgressPercent(status: string | undefined): number {
  switch (status) {
    case 'COMPLETED':
      return 100;
    case 'IN_PROGRESS':
      return 60;
    case 'PENDING':
      return 20;
    case 'FAILED':
      return 100;
    default:
      return 0;
  }
}

export const JOB_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendiente',
  IN_PROGRESS: 'En progreso',
  COMPLETED: 'Completado',
  FAILED: 'Fallido',
};
