import { customFetch } from '../../../lib/api/customFetch';
import type { UploadEvidenceResponse } from '../../../api/model';

export type UploadEvidenceInput = {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File;
};

type UploadEvidenceHttpResponse = {
  data: UploadEvidenceResponse;
  status: number;
  headers: Headers;
};

/**
 * Multipart UC-004: parts `file`, `criterionId`, `description`
 * (el cliente Orval enviaba criterionId/description como query).
 */
export async function uploadEvidence(
  input: UploadEvidenceInput,
): Promise<UploadEvidenceHttpResponse> {
  const formData = new FormData();
  formData.append('file', input.file);
  formData.append('criterionId', input.criterionId);
  formData.append('description', input.description);

  return customFetch<UploadEvidenceHttpResponse>(
    `/api/v1/indicators/${encodeURIComponent(input.indicatorId)}/evidences`,
    {
      method: 'POST',
      body: formData,
    },
  );
}
