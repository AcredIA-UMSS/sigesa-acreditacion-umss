import { customFetch } from '../../../lib/api/customFetch';

export interface TechnicianInboxItemDto {
  indicatorId: string;
  indicatorCode: string;
  indicatorDescription: string;
  status: string;
  dimensionName: string;
  criterionName: string;
  subcriterionName: string;
  pathHierarchy: string;
  programId: string;
  programName: string;
  evidenceId: string | null;
  evidenceFilename: string | null;
  evidenceUploadedAt: string | null;
  activeObservationsCount: number;
  updatedAt: string;
}

export interface PaginatedTechnicianInboxResponse {
  content: TechnicianInboxItemDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface TechnicianInboxQueryParams {
  programId?: string;
  level1Id?: string;
  status?: string;
  page?: number;
  size?: number;
}

export async function fetchTechnicianInbox(
  params: TechnicianInboxQueryParams = {},
  signal?: AbortSignal,
): Promise<PaginatedTechnicianInboxResponse> {
  const query = new URLSearchParams();
  if (params.programId) query.set('programId', params.programId);
  if (params.level1Id) query.set('level1Id', params.level1Id);
  if (params.status) query.set('status', params.status);
  query.set('page', String(params.page ?? 0));
  query.set('size', String(params.size ?? 20));

  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  const res = await customFetch<{ data: PaginatedTechnicianInboxResponse }>(
    `/api/v1/dashboards/technician/inbox?${query.toString()}`,
    {
      method: 'GET',
      signal: timeoutSignal,
    },
  );
  return res.data;
}

export async function approveIndicator(
  indicatorId: string,
  signal?: AbortSignal,
): Promise<void> {
  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  await customFetch<void>(`/api/v1/indicators/${indicatorId}/approve`, {
    method: 'POST',
    signal: timeoutSignal,
  });
}

export async function rejectIndicator(
  indicatorId: string,
  justification: string,
  signal?: AbortSignal,
): Promise<void> {
  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  await customFetch<void>(`/api/v1/indicators/${indicatorId}/reject`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ justification }),
    signal: timeoutSignal,
  });
}
