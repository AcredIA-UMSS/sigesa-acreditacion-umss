import { useState } from 'react';
import { customFetch } from '../../../lib/api/customFetch';
import { resolveAccessToken } from '../../../lib/auth/authBridge';

export interface ExportJobResponse {
  jobId: string;
  status: string;
  message: string;
  statusUrl: string;
}

export interface JobStatusResponse {
  jobId: string;
  status: string;
  progressPercentage: number;
  downloadUrl: string | null;
  errorMessage: string | null;
}

export async function downloadDossierFile(processId: string, jobId: string, downloadUrl?: string | null): Promise<void> {
  // If downloadUrl is a direct presigned S3 URL, trigger browser direct download without backend proxying
  if (downloadUrl && (downloadUrl.startsWith('http://') || downloadUrl.startsWith('https://'))) {
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.download = `${processId.substring(0, 8)}.zip`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    return;
  }

  const token = resolveAccessToken();
  const headers = new Headers();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const endpoint = downloadUrl && downloadUrl.startsWith('/')
    ? downloadUrl
    : `/api/v1/processes/${processId}/dossier/export-jobs/${jobId}/download`;

  const response = await fetch(endpoint, { headers });
  if (!response.ok) {
    throw new Error('No se pudo descargar el dossier.');
  }

  const disposition = response.headers.get('content-disposition') || response.headers.get('Content-Disposition');
  let filename = `${processId.substring(0, 8)}.zip`;
  if (disposition && disposition.includes('filename=')) {
    const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
    if (matches != null && matches[1]) {
      filename = matches[1].replace(/['"]/g, '');
    }
  }

  const responseData = await response.arrayBuffer();
  const blob = new Blob([responseData], { type: 'application/zip' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export function useDossierExport(processId: string) {
  const [isPending, setIsPending] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const exportDossier = async () => {
    setIsPending(true);
    setProgress(0);
    setError(null);

    try {
      const { data: jobData } = await customFetch<{ data: ExportJobResponse }>(`/api/v1/processes/${processId}/dossier/export`, {
        method: 'POST',
      });
      const jobId = jobData?.jobId;
      if (!jobId) throw new Error('No se recibió el ID de la exportación.');

      let jobStatus: JobStatusResponse['status'] = 'PENDING';
      let finalDownloadUrl: string | null = null;
      let attempts = 0;
      const maxAttempts = 330;

      while (jobStatus === 'PENDING' || jobStatus === 'PROCESSING') {
        if (attempts >= maxAttempts) {
          throw new Error('La generación del dossier excedió el límite de tiempo.');
        }
        await new Promise((resolve) => setTimeout(resolve, 2000));
        const { data: statusData } = await customFetch<{ data: JobStatusResponse }>(
          `/api/v1/processes/${processId}/dossier/export-jobs/${jobId}`,
        );
        jobStatus = statusData?.status ?? 'PENDING';
        setProgress(statusData?.progressPercentage ?? 0);
        finalDownloadUrl = statusData?.downloadUrl ?? null;
        if (jobStatus === 'FAILED') {
          throw new Error(statusData?.errorMessage ?? 'Error al procesar el dossier.');
        }
        attempts++;
      }

      await downloadDossierFile(processId, jobId, finalDownloadUrl);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Error al exportar el dossier.';
      setError(errorMessage);
      throw err;
    } finally {
      setIsPending(false);
    }
  };

  return { exportDossier, isPending, progress, error };
}
