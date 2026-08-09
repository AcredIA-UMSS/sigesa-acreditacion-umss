/**
 * Cliente de carga UC-004 con progreso (US-025).
 * Usa axios para `onUploadProgress`; el OpenAPI de EvidenceController está anotado
 * para regenerar Orval (`pnpm generate:api`) — este wrapper se mantiene por la barra de progreso.
 */
import axios from 'axios';
import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions, UseMutationResult } from '@tanstack/react-query';

import type { UploadEvidenceResponse } from '../../model';
import { loadSession } from '../../../lib/auth/tokenStorage';

/** Parámetros del cliente con progreso (US-025); no generado por Orval. */
export type UploadEvidenceParams = {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File;
};

const authHeaders = (): Record<string, string> => {
  const session = loadSession();
  return session?.accessToken
    ? { Authorization: `Bearer ${session.accessToken}` }
    : {};
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
