import { customFetch } from '../../../lib/api/customFetch';

export type SubphaseEvidenceItem = {
  evidenceId: string;
  subphaseId: string;
  indicatorId?: string;
  version: number;
  description: string;
  contentHash: string;
  originalFilename: string;
  uploadedAt: string;
  uploadedBy: string;
};

export type SubphaseObservationItem = {
  id: string;
  subphaseId: string;
  authorId: string;
  authorRole: string;
  body: string;
  status: 'OPEN' | 'RESOLVED';
  resolvedAt?: string;
  resolvedVersionId?: string;
  createdAt: string;
  updatedAt: string;
};

export type SubphaseSubsanationEligibility = {
  canSubsanate: boolean;
  openObservationId?: string;
  reason?: string;
};

export async function fetchSubphaseEvidences(
  subphaseId: string,
): Promise<SubphaseEvidenceItem[]> {
  const response = await customFetch<{ data: SubphaseEvidenceItem[] }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/evidences`,
  );
  return response.data;
}

export async function fetchSubphaseObservations(
  subphaseId: string,
): Promise<SubphaseObservationItem[]> {
  const response = await customFetch<{ data: SubphaseObservationItem[] }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/observations`,
  );
  return response.data;
}

export type UploadSubphaseEvidenceInput = {
  subphaseId: string;
  description: string;
  file: File;
};

export type SubphaseUploadResult = {
  evidenceId: string;
  version: number;
  contentHash: string;
  event?: string;
  currentState: string;
};

export async function uploadSubphaseEvidence(
  input: UploadSubphaseEvidenceInput,
): Promise<SubphaseUploadResult> {
  const formData = new FormData();
  formData.append('file', input.file);
  formData.append('description', input.description);

  const response = await customFetch<{
    data: SubphaseUploadResult;
    status: number;
  }>(`/api/v1/subphases/${encodeURIComponent(input.subphaseId)}/evidences`, {
    method: 'POST',
    body: formData,
  });

  return {
    evidenceId: response.data.evidenceId,
    version: response.data.version,
    contentHash: response.data.contentHash,
    event: response.data.event,
    currentState: response.data.currentState,
  };
}

export async function addSubphaseObservation(
  subphaseId: string,
  body: string,
): Promise<SubphaseObservationItem> {
  const response = await customFetch<{ data: SubphaseObservationItem }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/observations`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ body }),
    },
  );
  return response.data;
}

export async function fetchSubphaseSubsanationEligibility(
  subphaseId: string,
): Promise<SubphaseSubsanationEligibility> {
  const response = await customFetch<{ data: SubphaseSubsanationEligibility }>(
    `/api/v1/subphases/${encodeURIComponent(subphaseId)}/subsanation-eligibility`,
  );
  return response.data;
}

export type SubsanateSubphaseEvidenceInput = {
  subphaseId: string;
  evidenceId: string;
  observationId: string;
  description: string;
  file: File;
};

export async function subsanateSubphaseEvidence(
  input: SubsanateSubphaseEvidenceInput,
): Promise<{ evidenceId: string; version: number }> {
  const formData = new FormData();
  formData.append('file', input.file);
  formData.append('description', input.description);
  formData.append('observationId', input.observationId);

  const response = await customFetch<{
    data: { evidenceId: string; version: number };
    status: number;
  }>(
    `/api/v1/subphases/${encodeURIComponent(input.subphaseId)}/evidences/${encodeURIComponent(input.evidenceId)}/subsanate`,
    {
      method: 'POST',
      body: formData,
    },
  );

  return {
    evidenceId: response.data.evidenceId,
    version: response.data.version,
  };
}
