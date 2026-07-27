/**
 * Orval-compatible client — regenerar con `pnpm run generate:api` cuando el backend esté activo.
 */
import { useMutation, useQuery } from '@tanstack/react-query';
import type {
  UseMutationOptions,
  UseMutationResult,
  UseQueryOptions,
  UseQueryResult,
} from '@tanstack/react-query';

import type {
  GenerateExecutiveReportRequest,
  ReportJobAcceptedResponse,
  ReportJobStatusResponse,
} from '../../model';

const authHeaders = (): HeadersInit => {
  const token = localStorage.getItem('sigesa_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const getGenerateExecutiveReportUrl = () => '/api/v1/reports/executive/pdf';

export const generateExecutiveReport = async (
  body: GenerateExecutiveReportRequest,
  options?: RequestInit,
): Promise<ReportJobAcceptedResponse> => {
  const res = await fetch(getGenerateExecutiveReportUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(), ...options?.headers },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`Report generation failed: ${res.status}`);
  }
  return res.json() as Promise<ReportJobAcceptedResponse>;
};

export const useGenerateExecutiveReport = (
  options?: UseMutationOptions<
    ReportJobAcceptedResponse,
    Error,
    { data: GenerateExecutiveReportRequest }
  >,
): UseMutationResult<
  ReportJobAcceptedResponse,
  Error,
  { data: GenerateExecutiveReportRequest }
> =>
  useMutation({
    mutationFn: ({ data }) => generateExecutiveReport(data),
    ...options,
  });

export const getReportJobStatusUrl = (jobId: string) =>
  `/api/v1/reports/executive/pdf/${jobId}`;

export const fetchReportJobStatus = async (
  jobId: string,
  options?: RequestInit,
): Promise<ReportJobStatusResponse> => {
  const res = await fetch(getReportJobStatusUrl(jobId), {
    ...options,
    headers: { ...authHeaders(), ...options?.headers },
  });
  if (!res.ok) {
    throw new Error(`Status poll failed: ${res.status}`);
  }
  return res.json() as Promise<ReportJobStatusResponse>;
};

export const useReportJobStatus = (
  jobId: string | null,
  options?: Omit<UseQueryOptions<ReportJobStatusResponse, Error>, 'queryKey' | 'queryFn'>,
): UseQueryResult<ReportJobStatusResponse, Error> =>
  useQuery({
    queryKey: ['reportJobStatus', jobId],
    queryFn: () => fetchReportJobStatus(jobId!),
    enabled: jobId !== null,
    refetchInterval: (query) =>
      query.state.data?.status === 'COMPLETED' || query.state.data?.status === 'FAILED'
        ? false
        : 2000,
    ...options,
  });

export const getReportDownloadUrl = (jobId: string) =>
  `/api/v1/reports/executive/pdf/${jobId}/download`;

export const downloadReportPdf = async (jobId: string): Promise<void> => {
  const res = await fetch(getReportDownloadUrl(jobId), {
    headers: authHeaders(),
  });
  if (!res.ok) {
    throw new Error(`Download failed: ${res.status}`);
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `sigesa-reporte-ejecutivo-${jobId}.pdf`;
  anchor.click();
  URL.revokeObjectURL(url);
};
