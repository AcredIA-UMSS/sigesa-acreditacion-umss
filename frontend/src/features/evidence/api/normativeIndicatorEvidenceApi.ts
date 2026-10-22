import { listByProcess } from '../../../api/endpoints/document-assets/document-assets';
import { linkDocumentAsset, listEvidences, uploadEvidence } from '../../../api/endpoints/evidence/evidence';
import type { DocumentAssetResponseDto } from '../../../api/model/documentAssetResponseDto';
import type { NormativeIndicatorEvidenceResponseDto } from '../../../api/model/normativeIndicatorEvidenceResponseDto';
import type { UploadEvidenceResponse } from '../../../api/model/uploadEvidenceResponse';
import { customFetch } from '../../../lib/api/customFetch';

export type NormativeIndicatorEvidenceItem = NormativeIndicatorEvidenceResponseDto & {
  evidenceId: string;
  indicatorId: string;
  version: number;
  description: string;
  contentHash: string;
  originalFilename: string;
  uploadedAt: string;
  uploadedBy: string;
};

export type UploadNormativeIndicatorEvidenceInput = {
  indicatorId: string;
  description: string;
  file?: File;
  externalUrl?: string;
};

export type NormativeIndicatorUploadResult = UploadEvidenceResponse;

export type ProcessDocumentAsset = DocumentAssetResponseDto;

export type NormativeIndicatorSubsanationEligibility = {
  canSubsanate: boolean;
  openObservationId?: string;
  reason?: string;
};

export async function fetchNormativeIndicatorEvidences(
  indicatorId: string,
): Promise<NormativeIndicatorEvidenceItem[]> {
  const response = await listEvidences(indicatorId, { skipUnauthorizedLogout: true });
  return (response.data ?? []).map((item) => ({
    ...item,
    evidenceId: item.evidenceId ?? '',
    indicatorId: item.indicatorId ?? '',
    version: item.version ?? 0,
    description: item.description ?? '',
    contentHash: item.contentHash ?? '',
    originalFilename: item.originalFilename ?? 'evidencia',
    uploadedAt: item.uploadedAt ?? '',
    uploadedBy: item.uploadedBy ?? '',
  }));
}

export async function uploadNormativeIndicatorEvidence(
  input: UploadNormativeIndicatorEvidenceInput,
): Promise<NormativeIndicatorUploadResult> {
  const response = await uploadEvidence(
    input.indicatorId,
    {
      description: input.description,
      externalUrl: input.externalUrl,
    },
    input.file ? { file: input.file } : undefined,
    { skipUnauthorizedLogout: true },
  );
  return response.data;
}

export async function fetchProcessDocumentAssets(
  processId: string,
): Promise<ProcessDocumentAsset[]> {
  const response = await listByProcess({ processId }, { skipUnauthorizedLogout: true });
  return response.data ?? [];
}

export async function linkDocumentAssetToIndicator(input: {
  indicatorId: string;
  documentAssetId: string;
}): Promise<NormativeIndicatorUploadResult> {
  const response = await linkDocumentAsset(
    input.indicatorId,
    { documentAssetId: input.documentAssetId },
    { skipUnauthorizedLogout: true },
  );
  return { ...response.data, reusedAsset: response.data.reusedAsset ?? true };
}

export async function deleteNormativeIndicatorEvidence(evidenceId: string): Promise<void> {
  await customFetch(`/api/v1/evidences/${encodeURIComponent(evidenceId)}`, {
    method: 'DELETE',
    skipUnauthorizedLogout: true,
  });
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
