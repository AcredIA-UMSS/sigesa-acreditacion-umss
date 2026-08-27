import { customFetch } from '../../../lib/api/customFetch';
import type { SubphaseUploadResult } from '../../subphases/api/subphaseApi';

export type UploadEvidenceInput = {
  subphaseId: string;
  description: string;
  file: File;
};

/**
 * Carga UC-004 por subfase (reemplaza el endpoint legacy por indicador).
 */
export async function uploadEvidence(
  input: UploadEvidenceInput,
): Promise<SubphaseUploadResult> {
  const formData = new FormData();
  formData.append('file', input.file);
  formData.append('description', input.description);

  const response = await customFetch<{ data: SubphaseUploadResult }>(
    `/api/v1/subphases/${encodeURIComponent(input.subphaseId)}/evidences`,
    {
      method: 'POST',
      body: formData,
    },
  );

  return response.data;
}
