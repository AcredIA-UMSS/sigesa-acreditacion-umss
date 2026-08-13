import { useCallback, useState } from 'react';
import {
  download,
  useGenerate,
  useGetStatus,
} from '../../../api/endpoints/report-controller/report-controller';
import type {
  GenerateExecutiveReportRequest,
  ReportJobStatusResponse,
} from '../../../api/model';
import { mapReportError } from './mapReportError';

export type ExecutiveReportFormState = {
  facultyId: string;
  programId: string;
  managementYear: number;
};

export type ExecutiveReportField = keyof ExecutiveReportFormState;

export type ExecutiveReportValidationErrors = Partial<
  Record<ExecutiveReportField, string>
>;

const defaultForm: ExecutiveReportFormState = {
  facultyId: '',
  programId: '',
  managementYear: new Date().getFullYear(),
};

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function validateForm(
  form: ExecutiveReportFormState,
): ExecutiveReportValidationErrors {
  const errors: ExecutiveReportValidationErrors = {};

  if (!Number.isInteger(form.managementYear) || form.managementYear < 2000) {
    errors.managementYear = 'Indique un año de gestión válido.';
  }

  const facultyId = form.facultyId.trim();
  if (facultyId && !UUID_PATTERN.test(facultyId)) {
    errors.facultyId = 'Indique un UUID de facultad válido.';
  }

  const programId = form.programId.trim();
  if (programId && !UUID_PATTERN.test(programId)) {
    errors.programId = 'Indique un UUID de programa válido.';
  }

  return errors;
}

function toPayload(form: ExecutiveReportFormState): GenerateExecutiveReportRequest {
  const payload: GenerateExecutiveReportRequest = {
    managementYear: form.managementYear,
  };
  if (form.facultyId.trim()) {
    payload.facultyId = form.facultyId.trim();
  }
  if (form.programId.trim()) {
    payload.programId = form.programId.trim();
  }
  return payload;
}

async function downloadReportPdf(jobId: string): Promise<void> {
  const response = await download(jobId);
  const fileData = response.data as unknown;
  if (response.status !== 200 || !(fileData instanceof Blob)) {
    throw new Error('Download failed: REPORT_NOT_READY');
  }
  const url = window.URL.createObjectURL(fileData);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `reporte-ejecutivo-${jobId}.pdf`;
  anchor.click();
  window.URL.revokeObjectURL(url);
}

export function useExecutiveReport() {
  const [form, setForm] = useState<ExecutiveReportFormState>(defaultForm);
  const [activeJobId, setActiveJobId] = useState<string | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] =
    useState<ExecutiveReportValidationErrors>({});

  const generateMutation = useGenerate({
    mutation: {
      onSuccess: (response) => {
        setActiveJobId(response.data.jobId ?? null);
        setValidationErrors({});
        setDownloadError(null);
      },
    },
  });

  const statusQuery = useGetStatus(activeJobId ?? '', {
    query: {
      enabled: activeJobId !== null,
      refetchInterval: (query) => {
        const status = query.state.data?.data?.status;
        if (status === 'COMPLETED' || status === 'FAILED') {
          return false;
        }
        return 2000;
      },
    },
  });

  const jobStatus: ReportJobStatusResponse | undefined =
    statusQuery.data?.status === 200 ? statusQuery.data.data : undefined;

  const updateField = useCallback(
    <K extends ExecutiveReportField>(
      key: K,
      value: ExecutiveReportFormState[K],
    ) => {
      setForm((prev) => ({ ...prev, [key]: value }));
      setValidationErrors((prev) => {
        if (!prev[key]) return prev;
        const next = { ...prev };
        delete next[key];
        return next;
      });
    },
    [],
  );

  const submit = useCallback(() => {
    const errors = validateForm(form);
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setActiveJobId(null);
    setDownloadError(null);
    generateMutation.reset();
    generateMutation.mutate({ data: toPayload(form) });
  }, [form, generateMutation]);

  const downloadPdf = useCallback(async () => {
    if (!activeJobId) return;
    setIsDownloading(true);
    setDownloadError(null);
    try {
      await downloadReportPdf(activeJobId);
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'Error al descargar el PDF';
      setDownloadError(mapReportError(message));
    } finally {
      setIsDownloading(false);
    }
  }, [activeJobId]);

  const reset = useCallback(() => {
    setForm(defaultForm);
    setActiveJobId(null);
    setDownloadError(null);
    setValidationErrors({});
    generateMutation.reset();
  }, [generateMutation]);

  const isPolling =
    activeJobId !== null &&
    jobStatus?.status !== 'COMPLETED' &&
    jobStatus?.status !== 'FAILED';

  const submitErrorMessage = mapReportError(
    generateMutation.error instanceof Error ? generateMutation.error.message : null,
  );
  const statusErrorMessage = mapReportError(
    statusQuery.error instanceof Error ? statusQuery.error.message : null,
  );

  return {
    form,
    updateField,
    submit,
    download: downloadPdf,
    reset,
    validationErrors,
    submitErrorMessage,
    statusErrorMessage,
    downloadErrorMessage: downloadError,
    jobStatus,
    activeJobId,
    isSubmitting: generateMutation.isPending,
    isDownloading,
    isPolling,
    isBlocked: generateMutation.isPending || isPolling,
  };
}
