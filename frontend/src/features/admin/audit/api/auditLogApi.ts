import { customFetch } from '../../../../lib/api/customFetch';
import { resolveAccessToken } from '../../../../lib/auth/authBridge';

export interface AuditLogItemDto {
  id: string;
  actorId: string | null;
  actorEmail: string;
  actorRole: string | null;
  action: string;
  entityType: string;
  entityId: string;
  payloadDiff: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface PaginatedAuditLogResponse {
  content: AuditLogItemDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface AuditLogQueryParams {
  actorEmail?: string;
  action?: string;
  entityType?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export async function fetchAuditLogs(
  params: AuditLogQueryParams = {},
  signal?: AbortSignal,
): Promise<PaginatedAuditLogResponse> {
  const query = new URLSearchParams();
  if (params.actorEmail) query.set('actorEmail', params.actorEmail);
  if (params.action) query.set('action', params.action);
  if (params.entityType) query.set('entityType', params.entityType);
  if (params.from) query.set('from', params.from);
  if (params.to) query.set('to', params.to);
  query.set('page', String(params.page ?? 0));
  query.set('size', String(params.size ?? 20));

  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  const res = await customFetch<{ data: PaginatedAuditLogResponse }>(
    `/api/v1/audit/logs?${query.toString()}`,
    {
      method: 'GET',
      signal: timeoutSignal,
    },
  );
  return res.data;
}

export async function downloadAuditLogsCsv(
  params: Omit<AuditLogQueryParams, 'page' | 'size'> = {},
): Promise<void> {
  const query = new URLSearchParams();
  if (params.actorEmail) query.set('actorEmail', params.actorEmail);
  if (params.action) query.set('action', params.action);
  if (params.entityType) query.set('entityType', params.entityType);
  if (params.from) query.set('from', params.from);
  if (params.to) query.set('to', params.to);

  const token = resolveAccessToken();
  const headers = new Headers();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`/api/v1/audit/logs/export?${query.toString()}`, {
    method: 'GET',
    headers,
    signal: AbortSignal.timeout(15000),
  });

  if (!response.ok) {
    throw new Error('No se pudo descargar el archivo de auditoría.');
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `bitacora_auditoria_${new Date().toISOString().slice(0, 10)}.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}
