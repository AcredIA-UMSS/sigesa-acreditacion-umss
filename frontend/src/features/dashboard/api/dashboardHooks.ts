import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
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
import { mockPersonas, mockObservations } from '../../../mocks/dashboardFixtures';

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
    csv: 'text/csv;charset=utf-8;'
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
  const queryClient = useQueryClient();
  const [mockPersona, setMockPersonaState] = useState<string | null>(() => {
    return localStorage.getItem('sigesa_mock_dashboard_persona');
  });

  const query = useQuery({
    queryKey: ['/api/v1/dashboards/me/summary', mockPersona],
    queryFn: async () => {
      if (mockPersona && mockPersona in mockPersonas) {
        // Return mock data after a tiny artificial delay to maintain realistic UX loading state
        await new Promise((resolve) => setTimeout(resolve, 300));
        return mockPersonas[mockPersona as keyof typeof mockPersonas];
      }
      const response = await getCompositeSummary();
      return mapCompositeSummary(response.data);
    },
  });

  const changeMockPersona = (newPersona: string | null) => {
    if (newPersona) {
      localStorage.setItem('sigesa_mock_dashboard_persona', newPersona);
    } else {
      localStorage.removeItem('sigesa_mock_dashboard_persona');
    }
    setMockPersonaState(newPersona);
    // Invalidate dashboard details to match the selected persona
    queryClient.invalidateQueries({ queryKey: ['/api/v1/dashboards/coordinator/details'] });
  };

  return {
    ...query,
    summary: query.data as CompositeSummaryResponse | undefined,
    mockPersona,
    changeMockPersona,
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
  const mockPersona = localStorage.getItem('sigesa_mock_dashboard_persona');
  const isMockActive = mockPersona && mockPersona in mockPersonas;

  // Convert Spanish state/params to English backend expectations
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
    queryKey: ['/api/v1/dashboards/coordinator/details', backendParams, isMockActive],
    queryFn: async () => {
      if (isMockActive) {
        await new Promise((resolve) => setTimeout(resolve, 200));
        let filtered = [...mockObservations];
        if (params.estado) {
          filtered = filtered.filter((o) => o.estado === params.estado);
        }
        if (params.phaseId) {
          if (params.phaseId === 1) {
            filtered = filtered.filter((o) => o.codigoIndicador.startsWith('IND-1.') || o.codigoIndicador.startsWith('IND-2.'));
          } else if (params.phaseId === 2) {
            filtered = filtered.filter((o) => !o.codigoIndicador.startsWith('IND-1.') && !o.codigoIndicador.startsWith('IND-2.'));
          }
        }
        if (params.sort) {
          const [field, order] = params.sort.split(',');
          filtered.sort((a, b) => {
            if (field === 'dueDate' || field === 'fechaLimite') {
              return order === 'asc'
                ? a.fechaLimite.localeCompare(b.fechaLimite)
                : b.fechaLimite.localeCompare(a.fechaLimite);
            }
            return 0;
          });
        }
        const pageNum = params.page ?? 0;
        const pageSize = params.size ?? 5;
        const totalElements = filtered.length;
        const totalPages = Math.ceil(totalElements / pageSize);
        const paginatedContent = filtered.slice(pageNum * pageSize, (pageNum + 1) * pageSize);

        return {
          content: paginatedContent,
          totalElements,
          totalPages,
          size: pageSize,
          number: pageNum,
        };
      }

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
    const mockPersona = localStorage.getItem('sigesa_mock_dashboard_persona');
    const isMockActive = mockPersona && mockPersona in mockPersonas;

    setIsPending(true);
    setProgress(0);
    setError(null);

    try {
      if (isMockActive) {
        // Simulate progress: 0% -> 50% -> 100% over 1.5 seconds
        await new Promise((resolve) => setTimeout(resolve, 500));
        setProgress(50);
        await new Promise((resolve) => setTimeout(resolve, 500));
        setProgress(100);
        await new Promise((resolve) => setTimeout(resolve, 500));

        let filteredObs = [...mockObservations];
        if (phaseId) {
          if (phaseId === 1) {
            filteredObs = filteredObs.filter((o) => o.codigoIndicador.startsWith('IND-1.') || o.codigoIndicador.startsWith('IND-2.'));
          } else if (phaseId === 2) {
            filteredObs = filteredObs.filter((o) => !o.codigoIndicador.startsWith('IND-1.') && !o.codigoIndicador.startsWith('IND-2.'));
          }
        }

        const headers = "ID,Indicador,Título,Estado,Fecha Límite\n";
        const rows = filteredObs
          .map(obs => `"${obs.observacionId}","${obs.codigoIndicador}","${obs.tituloIndicador}","${obs.estado}","${obs.fechaLimite}"`)
          .join("\n");
        const fullCsv = headers + rows;

        let blob: Blob;
        let filename = `reporte_coordinador_${new Date().toISOString().slice(0, 10)}`;

        if (format === 'csv') {
          blob = new Blob([fullCsv], { type: 'text/csv;charset=utf-8;' });
          filename += '.csv';
        } else {
          alert("Modo Simulación: Descarga binaria de Excel/PDF interceptada. En el entorno local se generará un archivo CSV para inspección de datos.");
          blob = new Blob([fullCsv], { type: 'text/csv;charset=utf-8;' });
          filename += '.csv';
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        setIsPending(false);
        return;
      }

      // 1. Enqueue job
      const cleanPhaseId = phaseId === undefined || phaseId === 0 ? undefined : phaseId;
      const response = await enqueueExportJob({ format, phaseId: cleanPhaseId });
      const jobId = response.data.jobId;
      if (!jobId) throw new Error('No se recibió el ID de la exportación.');

      // 2. Poll job status
      let jobStatus = 'PENDING';
      let attempts = 0;
      const maxAttempts = 60; // 60 seconds timeout

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

      // 3. Download file
      const ext = format === 'csv' ? 'csv' : (format === 'pdf' ? 'pdf' : 'xlsx');
      await downloadReportFile(jobId, `reporte_coordinador.${ext}`, format);
    } catch (err: any) {
      setError(err.message ?? 'Error al exportar.');
      throw err;
    } finally {
      setIsPending(false);
    }
  };

  return { exportReport, isPending, progress, error };
}
