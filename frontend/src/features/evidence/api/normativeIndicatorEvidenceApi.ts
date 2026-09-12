import { customFetch } from '../../../lib/api/customFetch';

export type NormativeIndicatorEvidenceItem = {
  evidenceId: string;
  indicatorId: string;
  version: number;
  description: string;
  contentHash: string;
  originalFilename: string;
  externalUrl?: string;
  uploadedAt: string;
  uploadedBy: string;
};

export type UploadNormativeIndicatorEvidenceInput = {
  indicatorId: string;
  description: string;
  file?: File;
  externalUrl?: string;
};

export type NormativeIndicatorUploadResult = {
  evidenceId: string;
  version: number;
  contentHash: string;
  event?: string;
  currentState: string;
};

export type NormativeIndicatorSubsanationEligibility = {
  canSubsanate: boolean;
  openObservationId?: string;
  reason?: string;
};

export async function fetchNormativeIndicatorEvidences(
  indicatorId: string,
): Promise<NormativeIndicatorEvidenceItem[]> {
  const response = await customFetch<{ data: NormativeIndicatorEvidenceItem[] }>(
    `/api/v1/indicators/${encodeURIComponent(indicatorId)}/evidences`,
    { skipUnauthorizedLogout: true },
  );
  return response.data;
}

export async function uploadNormativeIndicatorEvidence(
  input: UploadNormativeIndicatorEvidenceInput,
): Promise<NormativeIndicatorUploadResult> {
  const formData = new FormData();
  if (input.file) {
    formData.append('file', input.file);
  }
  if (input.externalUrl?.trim()) {
    formData.append('externalUrl', input.externalUrl.trim());
  }
  formData.append('description', input.description);

  const response = await customFetch<{
    data: NormativeIndicatorUploadResult;
    status: number;
  }>(`/api/v1/indicators/${encodeURIComponent(input.indicatorId)}/evidences`, {
    method: 'POST',
    body: formData,
    // Evita logout global si el POST falla (p. ej. 401/403); la UI muestra el error.
    skipUnauthorizedLogout: true,
  });

  return {
    evidenceId: response.data.evidenceId,
    version: response.data.version,
    contentHash: response.data.contentHash,
    event: response.data.event,
    currentState: response.data.currentState,
  };
}

export async function fetchNormativeIndicatorSubsanationEligibility(
  indicatorId: string,
): Promise<NormativeIndicatorSubsanationEligibility> {
  const response = await customFetch<{ data: NormativeIndicatorSubsanationEligibility }>(
    `/api/v1/indicators/${encodeURIComponent(indicatorId)}/subsanation-eligibility`,
    { skipUnauthorizedLogout: true },
  );
  return response.data;
}

export type SubsanateNormativeIndicatorEvidenceInput = {
  indicatorId: string;
  evidenceId: string;
  observationId: string;
  description: string;
  file: File;
};

export async function subsanateNormativeIndicatorEvidence(
  input: SubsanateNormativeIndicatorEvidenceInput,
): Promise<{ evidenceId: string; version: number }> {
  const formData = new FormData();
  formData.append('file', input.file);
  formData.append('description', input.description);
  formData.append('observationId', input.observationId);

  const response = await customFetch<{
    data: { evidenceId: string; version: number };
    status: number;
  }>(
    `/api/v1/indicators/${encodeURIComponent(input.indicatorId)}/evidences/${encodeURIComponent(input.evidenceId)}/subsanate`,
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
