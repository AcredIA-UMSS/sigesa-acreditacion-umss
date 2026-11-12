import { customFetch } from '../../../../lib/api/customFetch';

export interface AccessSessionItem {
  id: string;
  userId: string | null;
  fullName: string | null;
  email: string;
  role: string | null;
  startedAt: string;
  endedAt: string | null;
  durationSeconds: number | null;
  ipAddress: string | null;
  userAgent: string | null;
  status: string;
  endReason: string | null;
  actions?: string[];
}

export interface AccessSessionPage {
  content: AccessSessionItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AccessSessionSummary {
  total: number;
  activeUsers: number;
  today: number;
  failed: number;
}

export interface AccessActionItem {
  id: string;
  createdAt: string;
  module: string;
  action: string;
  description: string | null;
  result: string;
  actorEmail: string | null;
  actorRole: string | null;
}

export interface AccessSessionQuery {
  email?: string;
  role?: string;
  status?: string;
  ipAddress?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export async function fetchAccessSummary(): Promise<AccessSessionSummary> {
  const response = await customFetch<{ data: AccessSessionSummary }>('/api/v1/access-sessions/summary');
  return response.data;
}

export async function fetchAccessSessions(params: AccessSessionQuery): Promise<AccessSessionPage> {
  const query = new URLSearchParams();
  if (params.email) query.set('email', params.email);
  if (params.role) query.set('role', params.role);
  if (params.status) query.set('status', params.status);
  if (params.ipAddress) query.set('ipAddress', params.ipAddress);
  if (params.from) query.set('from', params.from);
  if (params.to) query.set('to', params.to);
  query.set('page', String(params.page ?? 0));
  query.set('size', String(params.size ?? 20));
  query.set('sort', 'startedAt,desc');
  const response = await customFetch<{ data: AccessSessionPage }>(`/api/v1/access-sessions?${query.toString()}`);
  return response.data;
}

export async function fetchAccessActions(sessionId: string): Promise<AccessActionItem[]> {
  const response = await customFetch<{ data: AccessActionItem[] }>(`/api/v1/access-sessions/${sessionId}/actions`);
  return response.data;
}
