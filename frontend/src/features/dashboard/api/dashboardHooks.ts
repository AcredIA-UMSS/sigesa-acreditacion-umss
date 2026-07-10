import { useQuery } from '@tanstack/react-query';
import {
  getCompositeSummary,
  getCoordinatorDetails,
  enqueueExportJob,
  getJobStatus,
} from '../../../api/endpoints/dashboard-composite-controller/dashboard-composite-controller';
import {
  mapCompositeSummary,
  mapPaginatedObservations,
} from './dashboardMapper';
import type { CompositeSummaryResponse, PaginatedObservationsResponse } from '../types';
import { loadSession } from '../../../lib/auth/tokenStorage';
import { useState } from 'react';

/**
 * Downloads a file by jobId using the raw fetch API with appropriate Authorization headers.
 */
export async function downloadReportFile(jobId: string, defaultFilename: string, format: 'xlsx' | 'csv' | 'pdf') {
  const session = loadSession();
  const headers = new Headers();
  if (session?.accessToken) {
    headers.set('Authorization', `Bearer ${session.accessToken}`);
  }

  const response = await fetch(`/api/v1/dashboards/export-jobs/${jobId}/download`, { headers });
  if (!response.ok) {
    throw new Error('No se pudo descargar el archivo de reporte.');
  }

  const disposition = response.headers.get('content-disposition') || response.headers.get('Content-Disposition');
  let filename = defaultFilename || `reporte_coordinador_${new Date().toISOString().slice(0, 10)}.${format}`;
  if (disposition && disposition.includes('filename=')) {
    const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
    if (matches != null && matches[1]) {
      filename = matches[1].replace(/['"]/g, '');
    }
  }

  const responseData = await response.arrayBuffer();

  const MIME_TYPES: Record<string, string> = {
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    pdf: 'application/pdf',
    csv: 'text/csv;charset=utf-8;',
  };

  const blob = new Blob([responseData], { type: MIME_TYPES[format] || 'application/octet-stream' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

/**
 * Hook to retrieve the composite dashboard summary, mapping English API response to Spanish contract.
 */
export function useDashboardSummary() {
  const query = useQuery({
    queryKey: ['/api/v1/dashboards/me/summary'],
    queryFn: async () => {
      const response = await getCompositeSummary();
      return mapCompositeSummary(response.data);
    },
  });

  return {
    ...query,
    summary: query.data as CompositeSummaryResponse | undefined,
  };
}

/**
 * Hook to retrieve the paginated observations list, mapping English API response to Spanish contract.
 */
export function useDashboardDetails(params: {
  page?: number;
  size?: number;
  sort?: string;
  phaseId?: number;
  estado?: string;
}) {
  const backendParams = {
    phaseId: params.phaseId === undefined || params.phaseId === 0 ? undefined : params.phaseId,
    status: params.estado === undefined || params.estado === '' || params.estado === 'ALL' ? undefined : params.estado,
    pageable: {
      page: params.page,
      size: params.size,
      sort: params.sort ? [params.sort] : undefined,
    },
  };

  const query = useQuery({
    queryKey: ['/api/v1/dashboards/coordinator/details', backendParams],
    queryFn: async () => {
      const response = await getCoordinatorDetails(backendParams);
      return mapPaginatedObservations(response.data);
    },
  });

  return {
    ...query,
    details: query.data as PaginatedObservationsResponse | undefined,
  };
}

/**
 * Hook to manage the asynchronous export job lifecycle:
 * 1. Enqueue job
 * 2. Poll job status until COMPLETED or FAILED
 * 3. Trigger download of binary stream
 */
export function useExportReport() {
  const [isPending, setIsPending] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const exportReport = async (format: 'xlsx' | 'csv' | 'pdf', phaseId?: number) => {
    setIsPending(true);
    setProgress(0);
    setError(null);

    try {
      const cleanPhaseId = phaseId === undefined || phaseId === 0 ? undefined : phaseId;
      const response = await enqueueExportJob({ format, phaseId: cleanPhaseId });
      const jobId = response.data.jobId;
      if (!jobId) throw new Error('No se recibió el ID de la exportación.');

      let jobStatus = 'PENDING';
      let attempts = 0;
      const maxAttempts = 60;

      while (jobStatus === 'PENDING' || jobStatus === 'PROCESSING') {
        if (attempts >= maxAttempts) {
          throw new Error('La generación del reporte excedió el límite de tiempo.');
        }
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const statusResponse = await getJobStatus(jobId);
        jobStatus = statusResponse.data.status ?? 'PENDING';
        setProgress(statusResponse.data.progressPercentage ?? 0);

        if (jobStatus === 'FAILED') {
          throw new Error(statusResponse.data.errorMessage ?? 'Error al procesar el reporte.');
        }
        attempts++;
      }

      const ext = format === 'csv' ? 'csv' : format === 'pdf' ? 'pdf' : 'xlsx';
      await downloadReportFile(jobId, `reporte_coordinador.${ext}`, format);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Error al exportar.';
      setError(message);
      throw err;
    } finally {
      setIsPending(false);
    }
  };

  return { exportReport, isPending, progress, error };
}
