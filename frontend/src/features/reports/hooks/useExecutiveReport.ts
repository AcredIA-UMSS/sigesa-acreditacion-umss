import { useCallback, useState } from 'react';
import {
  downloadReportPdf,
  useGenerateExecutiveReport,
  useReportJobStatus,
} from '../../../api/endpoints/report-controller/report-controller';
import type { GenerateExecutiveReportRequest } from '../../../api/model';

export type ExecutiveReportFormState = {
  facultyId: string;
  programId: string;
  managementYear: number;
};

const defaultForm: ExecutiveReportFormState = {
  facultyId: '',
  programId: '',
  managementYear: new Date().getFullYear(),
};

export function useExecutiveReport() {
  const [form, setForm] = useState<ExecutiveReportFormState>(defaultForm);
  const [activeJobId, setActiveJobId] = useState<string | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);

  const generateMutation = useGenerateExecutiveReport({
    onSuccess: (data) => {
      setActiveJobId(data.jobId);
    },
  });

  const statusQuery = useReportJobStatus(activeJobId);

  const updateField = useCallback(
    <K extends keyof ExecutiveReportFormState>(key: K, value: ExecutiveReportFormState[K]) => {
      setForm((prev) => ({ ...prev, [key]: value }));
    },
    [],
  );

  const submit = useCallback(() => {
    const payload: GenerateExecutiveReportRequest = {
      managementYear: form.managementYear,
    };
    if (form.facultyId.trim()) {
      payload.facultyId = form.facultyId.trim();
    }
    if (form.programId.trim()) {
      payload.programId = form.programId.trim();
    }
    generateMutation.mutate({ data: payload });
  }, [form, generateMutation]);

  const download = useCallback(async () => {
    if (!activeJobId) return;
    setIsDownloading(true);
    try {
      await downloadReportPdf(activeJobId);
    } finally {
      setIsDownloading(false);
    }
  }, [activeJobId]);

  return {
    form,
    updateField,
    submit,
    download,
    isSubmitting: generateMutation.isPending,
    isDownloading,
    submitError: generateMutation.error,
    jobStatus: statusQuery.data,
    isPolling: statusQuery.isFetching,
    reset: () => {
      setActiveJobId(null);
      setForm(defaultForm);
    },
  };
}
