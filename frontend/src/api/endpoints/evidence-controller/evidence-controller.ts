/**
 * Orval-compatible client — regenerar con `pnpm run generate:api` cuando el backend esté activo.
 */
import axios from 'axios';
import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions, UseMutationResult } from '@tanstack/react-query';

import type { UploadEvidenceParams, UploadEvidenceResponse } from '../../model';

const authHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('sigesa_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const getUploadEvidenceUrl = (indicatorId: string) =>
  `/api/v1/indicators/${indicatorId}/evidences`;

export type UploadProgressHandler = (percent: number) => void;

export const uploadEvidence = async (
  params: UploadEvidenceParams,
  onProgress?: UploadProgressHandler,
): Promise<UploadEvidenceResponse> => {
  const formData = new FormData();
  formData.append('file', params.file);
  formData.append('criterionId', params.criterionId);
  formData.append('description', params.description);

  const response = await axios.post<UploadEvidenceResponse>(
    getUploadEvidenceUrl(params.indicatorId),
    formData,
    {
      headers: {
        ...authHeaders(),
      },
      onUploadProgress: (event) => {
        if (!onProgress || !event.total) return;
        onProgress(Math.round((event.loaded * 100) / event.total));
      },
    },
  );
  return response.data;
};

export const useUploadEvidence = (
  options?: UseMutationOptions<
    UploadEvidenceResponse,
    Error,
    { data: UploadEvidenceParams; onProgress?: UploadProgressHandler }
  >,
): UseMutationResult<
  UploadEvidenceResponse,
  Error,
  { data: UploadEvidenceParams; onProgress?: UploadProgressHandler }
> =>
  useMutation({
    mutationFn: ({ data, onProgress }) => uploadEvidence(data, onProgress),
    ...options,
  });

/** Umbral FSD-BR-18 / US-025: 5 MB */
export const LARGE_FILE_THRESHOLD_BYTES = 5 * 1024 * 1024;
