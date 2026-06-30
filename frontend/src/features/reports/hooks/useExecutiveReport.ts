import { useCallback, useState } from 'react';
import {
  downloadReportPdf,
  useGenerateExecutiveReport,
  useReportJobStatus,
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

function validateForm(
  form: ExecutiveReportFormState,
): ExecutiveReportValidationErrors {
  const errors: ExecutiveReportValidationErrors = {};

  if (!Number.isInteger(form.managementYear) || form.managementYear < 2000) {
    errors.managementYear = 'Indique un año de gestión válido.';
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

export function useExecutiveReport() {
  const [form, setForm] = useState<ExecutiveReportFormState>(defaultForm);
  const [activeJobId, setActiveJobId] = useState<string | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] =
    useState<ExecutiveReportValidationErrors>({});

  const generateMutation = useGenerateExecutiveReport({
    onSuccess: (data) => {
      setActiveJobId(data.jobId);
      setValidationErrors({});
      setDownloadError(null);
    },
  });

  const statusQuery = useReportJobStatus(activeJobId);

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

  const download = useCallback(async () => {
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
    statusQuery.data?.status !== 'COMPLETED' &&
    statusQuery.data?.status !== 'FAILED';

  const submitErrorMessage = mapReportError(generateMutation.error?.message);
  const statusErrorMessage = mapReportError(statusQuery.error?.message);

  return {
    form,
    updateField,
    submit,
    download,
    reset,
    validationErrors,
    submitErrorMessage,
    statusErrorMessage,
    downloadErrorMessage: downloadError,
    jobStatus: statusQuery.data as ReportJobStatusResponse | undefined,
    activeJobId,
    isSubmitting: generateMutation.isPending,
    isDownloading,
    isPolling,
    isBlocked: generateMutation.isPending || isPolling,
  };
}
