import { customFetch } from '../../../lib/api/customFetch';

export type EvidenceVersionHistoryItem = {
  versionId: string;
  version: number;
  supersedesVersion?: number;
  observationId?: string;
  description: string;
  contentHash: string;
  originalFilename?: string;
  createdBy: string;
  createdAt: string;
  current: boolean;
  blobAvailable: boolean;
};

export async function fetchEvidenceVersions(
  evidenceId: string,
): Promise<EvidenceVersionHistoryItem[]> {
  const response = await customFetch<{ data: EvidenceVersionHistoryItem[] }>(
    `/api/v1/evidences/${encodeURIComponent(evidenceId)}/versions`,
  );
  return response.data;
}
