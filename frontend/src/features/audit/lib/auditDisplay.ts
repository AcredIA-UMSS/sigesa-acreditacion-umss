import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { isApiError } from '../../../lib/api/apiError';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import type { AuditHistoryView } from '../historyRoutes';

export interface ActorProfile {
  fullName: string;
  email: string;
}

export interface ActorDirectory {
  byId: ReadonlyMap<string, ActorProfile>;
  byEmail: ReadonlyMap<string, ActorProfile>;
}

const ACTION_LABELS: Record<string, string> = {
  LOGIN: 'Inicio de sesión',
  LOGOUT: 'Cierre de sesión',
  SESSION_EXPIRED: 'Sesión expirada',
  USER_REGISTERED: 'Alta de usuario',
  USER_ACTIVATED: 'Activación de usuario',
  USER_DEACTIVATED: 'Desactivación de usuario',
  EVIDENCE_UPLOADED: 'Carga de evidencia',
  EVIDENCE_REPLACED: 'Reemplazo de evidencia',
  EVIDENCE_DELETED: 'Eliminación de evidencia',
  EVIDENCE_UPDATED: 'Modificación de evidencia',
  REPORT_REQUESTED: 'Solicitud de reporte',
};

const RESOURCE_LABELS: Record<string, string> = {
  USER: 'Cuentas',
  EVIDENCE: 'Evidencias',
  REPORT: 'Reportes',
};

const RESULT_LABELS: Record<string, string> = {
  SUCCESS: 'Exitoso',
  FAILURE: 'Fallido',
};

const PAYLOAD_LABELS: Record<string, string> = {
  indicatorId: 'Indicador',
  programId: 'Proceso',
  criterionId: 'Criterio',
  processId: 'Proceso',
  fileName: 'Archivo',
  filename: 'Archivo',
  role: 'Rol',
};

const SENSITIVE_KEY = /password|passwd|token|secret|credential|authorization|hash/i;

const RECORDED_EVIDENCE_ACTION = 'EVIDENCE_UPLOADED';

export const ACTION_OPTIONS = Object.entries(ACTION_LABELS)
  .filter(([value]) => !value.startsWith('EVIDENCE_') || value === RECORDED_EVIDENCE_ACTION)
  .map(([value, label]) => ({ value, label }));

export const EVIDENCE_ACTION_OPTIONS = ACTION_OPTIONS.filter(
  (option) => option.value === RECORDED_EVIDENCE_ACTION,
);

export const ACCOUNT_ACTION_OPTIONS = ACTION_OPTIONS.filter(
  (option) => option.value.startsWith('USER_') || option.value === 'REPORT_REQUESTED',
);

export const RESOURCE_OPTIONS = Object.entries(RESOURCE_LABELS).map(([value, label]) => ({ value, label }));

export const RESULT_OPTIONS = [
  { value: 'SUCCESS', label: 'Exitoso' },
  { value: 'FAILURE', label: 'Fallido' },
];

export type AuditColumnId =
  | 'date'
  | 'time'
  | 'user'
  | 'action'
  | 'resourceType'
  | 'resourceId'
  | 'description'
  | 'result'
  | 'ip';

const COLUMN_LABELS: Record<AuditColumnId, string> = {
  date: 'Fecha',
  time: 'Hora',
  user: 'Usuario',
  action: 'Acción',
  resourceType: 'Módulo',
  resourceId: 'Identificador',
  description: 'Descripción',
  result: 'Resultado',
  ip: 'IP',
};

export function columnsFor(viewId: AuditHistoryView['id']): AuditColumnId[] {
  if (viewId === 'access') {
    return ['date', 'time', 'user', 'ip', 'result', 'description'];
  }
  if (viewId === 'evidence') {
    return ['date', 'time', 'user', 'action', 'resourceId', 'description', 'result'];
  }
  return ['date', 'time', 'user', 'action', 'resourceType', 'resourceId', 'description', 'result'];
}

export function columnLabel(column: AuditColumnId): string {
  return COLUMN_LABELS[column];
}

export function actionLabel(action: string | undefined): string {
  if (!action) {
    return '—';
  }
  return ACTION_LABELS[action] ?? action;
}

export function resourceLabel(resourceType: string | undefined): string {
  if (!resourceType) {
    return '—';
  }
  return RESOURCE_LABELS[resourceType] ?? resourceType;
}

export function resultLabel(result: string | undefined): string {
  if (!result) {
    return '—';
  }
  return RESULT_LABELS[result] ?? result;
}

export function splitOccurredAt(occurredAt: string | undefined): { date: string; time: string } {
  if (!occurredAt) {
    return { date: '—', time: '—' };
  }
  const parsed = new Date(occurredAt);
  if (Number.isNaN(parsed.getTime())) {
    return { date: '—', time: '—' };
  }
  return {
    date: new Intl.DateTimeFormat('es-BO', {
      timeZone: 'America/La_Paz',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(parsed),
    time: new Intl.DateTimeFormat('es-BO', {
      timeZone: 'America/La_Paz',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hourCycle: 'h23',
    }).format(parsed),
  };
}

export function actorLabel(event: AuditLogResponse, directory: ActorDirectory): string {
  const email = event.actorEmail?.trim();
  if (email) {
    const known = directory.byEmail.get(email.toLowerCase());
    if (known && known.fullName !== '—') {
      return `${known.fullName} · ${email}`;
    }
    return email;
  }
  if (event.actorId) {
    const known = directory.byId.get(event.actorId);
    if (known) {
      return known.fullName !== '—' ? `${known.fullName} · ${known.email}` : known.email;
    }
  }
  return '—';
}

export function cellValue(
  column: AuditColumnId,
  event: AuditLogResponse,
  directory: ActorDirectory,
): string {
  const when = splitOccurredAt(event.occurredAt);
  switch (column) {
    case 'date':
      return when.date;
    case 'time':
      return when.time;
    case 'user':
      return actorLabel(event, directory);
    case 'action':
      return actionLabel(event.action);
    case 'resourceType':
      return resourceLabel(event.resourceType);
    case 'resourceId':
      return event.resourceId ?? '—';
    case 'description':
      return event.description?.trim() || '—';
    case 'result':
      return resultLabel(event.result);
    case 'ip':
      return event.ipAddress?.trim() || '—';
    default:
      return '—';
  }
}

export interface PayloadField {
  label: string;
  value: string;
}

export function safePayloadFields(payload: string | undefined): PayloadField[] {
  if (!payload?.trim()) {
    return [];
  }
  try {
    const parsed: unknown = JSON.parse(payload);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return [];
    }
    return Object.entries(parsed)
      .filter(([key]) => !SENSITIVE_KEY.test(key))
      .map(([key, value]) => ({
        label: PAYLOAD_LABELS[key] ?? key,
        value: formatPayloadValue(value),
      }))
      .filter((field) => field.value.length > 0);
  } catch {
    if (SENSITIVE_KEY.test(payload)) {
      return [];
    }
    return [{ label: 'Detalle', value: payload.slice(0, 500) }];
  }
}

function formatPayloadValue(value: unknown): string {
  if (typeof value === 'string') {
    return value;
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  if (value == null) {
    return '';
  }
  return JSON.stringify(value).slice(0, 500);
}

export function auditErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    if (error.status === 401) {
      return 'Sesión expirada o no autenticada. Inicie sesión nuevamente.';
    }
    if (error.status === 403) {
      return 'No tiene permiso para consultar el historial.';
    }
    return error.message;
  }
  return getApiErrorMessage(error, 'No se pudo consultar el historial.');
}
