import { customFetch } from '../../../lib/api/customFetch';

export type EvidenceSearchHit = {
  evidenceId: string;
  level1Id?: string;
  level1Name?: string;
  level3Name?: string;
  processId?: string;
  indicatorId?: string;
  indicatorCode?: string;
  indicatorTitle?: string;
  version: number;
  description: string;
  originalFilename?: string;
  uploadedAt: string;
  uploadedBy: string;
  blobAvailable: boolean;
};

export type EvidenceSearchPage = {
  items: EvidenceSearchHit[];
  total: number;
  page: number;
  size: number;
};

export type EvidenceSearchParams = {
  processId?: string;
  level1Id?: string;
  indicatorId?: string;
  programId?: string;
  q?: string;
  managementYear?: number;
  page?: number;
  size?: number;
};

function buildQuery(params: EvidenceSearchParams): string {
  const search = new URLSearchParams();
  if (params.processId) search.set('processId', params.processId);
  if (params.level1Id) search.set('level1Id', params.level1Id);
  if (params.indicatorId) search.set('indicatorId', params.indicatorId);
  if (params.programId) search.set('programId', params.programId);
  if (params.q?.trim()) search.set('q', params.q.trim());
  if (params.managementYear != null) {
    search.set('managementYear', String(params.managementYear));
  }
  search.set('page', String(params.page ?? 0));
  search.set('size', String(params.size ?? 20));
  return search.toString();
}

export async function fetchEvidenceSearch(
  params: EvidenceSearchParams,
): Promise<EvidenceSearchPage> {
  const query = buildQuery(params);
  const response = await customFetch<{ data: EvidenceSearchPage }>(
    `/api/v1/evidences/search?${query}`,
  );
  return response.data;
}
