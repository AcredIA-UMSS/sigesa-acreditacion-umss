import { customFetch } from '../../../lib/api/customFetch';

export type EvidenceSearchHit = {
  evidenceId: string;
  subphaseId?: string;
  subphaseName?: string;
  phaseId?: string;
  phaseName?: string;
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
  phaseId?: string;
  subphaseId?: string;
  indicatorId?: string;
  programId?: string;
  q?: string;
  aiEnabled?: boolean;
  managementYear?: number;
  page?: number;
  size?: number;
};

function buildQuery(params: EvidenceSearchParams): string {
  const search = new URLSearchParams();
  if (params.processId) search.set('processId', params.processId);
  if (params.phaseId) search.set('phaseId', params.phaseId);
  if (params.subphaseId) search.set('subphaseId', params.subphaseId);
  if (params.indicatorId) search.set('indicatorId', params.indicatorId);
  if (params.programId) search.set('programId', params.programId);
  if (params.q?.trim()) search.set('q', params.q.trim());
  if (params.aiEnabled != null) search.set('aiEnabled', String(params.aiEnabled));
  if (params.managementYear != null) {
    search.set('managementYear', String(params.managementYear));
  }
  search.set('page', String(params.page ?? 0));
  search.set('size', String(params.size ?? 20));
  return search.toString();
}

function mapItemToHit(item: any): EvidenceSearchHit {
  return {
    evidenceId: item.evidenceId ?? item.id ?? String(Math.random()),
    subphaseId: item.subphaseId,
    subphaseName: item.subphaseName,
    phaseId: item.phaseId,
    phaseName: item.phaseName ?? item.dimensionName,
    processId: item.processId,
    indicatorId: item.indicatorId,
    indicatorCode: item.indicatorCode ?? item.criterionCode,
    indicatorTitle: item.indicatorTitle,
    version: typeof item.version === 'number' ? item.version : 1,
    description: item.description ?? '',
    originalFilename: item.originalFilename ?? item.title ?? 'Evidencia sin nombre',
    uploadedAt: item.uploadedAt ?? new Date().toISOString(),
    uploadedBy: item.uploadedBy ?? item.carreraName ?? 'Sistema',
    blobAvailable: item.blobAvailable ?? true,
  };
}

export async function fetchEvidenceSearch(
  params: EvidenceSearchParams,
): Promise<EvidenceSearchPage> {
  const query = buildQuery(params);
  const headers: Record<string, string> = {};
  if (params.aiEnabled) {
    headers['X-AI-Enabled'] = 'true';
  }

  const response = await customFetch<any>(
    `/api/v1/evidences/search?${query}`,
    { headers },
  );

  let raw = response?.data;
  if (raw && typeof raw === 'object' && 'data' in raw && raw.data) {
    raw = raw.data;
  }

  const items: EvidenceSearchHit[] = [];

  if (raw && Array.isArray(raw.items)) {
    for (const item of raw.items) {
      if (item) {
        items.push(mapItemToHit(item));
      }
    }
  } else if (raw && Array.isArray(raw.subsets)) {
    for (const subset of raw.subsets) {
      if (subset && Array.isArray(subset.results)) {
        for (const item of subset.results) {
          if (item) {
            items.push(mapItemToHit(item));
          }
        }
      }
    }
  } else if (Array.isArray(raw)) {
    for (const item of raw) {
      if (item) {
        items.push(mapItemToHit(item));
      }
    }
  }

  const total = typeof raw?.total === 'number' ? raw.total : items.length;
  const page = typeof raw?.page === 'number' ? raw.page : (params.page ?? 0);
  const size = typeof raw?.size === 'number' ? raw.size : (params.size ?? 20);

  return {
    items,
    total,
    page,
    size,
  };
}
