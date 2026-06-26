import { useCallback, useState } from 'react';
import {
  LARGE_FILE_THRESHOLD_BYTES,
  useUploadEvidence,
} from '../../../api/endpoints/evidence-controller/evidence-controller';
import type { UploadEvidenceResponse } from '../../../api/model';

export type EvidenceUploadForm = {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File | null;
};

const defaultForm: EvidenceUploadForm = {
  indicatorId: '550e8400-e29b-41d4-a716-446655440003',
  criterionId: '550e8400-e29b-41d4-a716-446655440002',
  description: '',
  file: null,
};

export function useEvidenceUpload() {
  const [form, setForm] = useState<EvidenceUploadForm>(defaultForm);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<UploadEvidenceResponse | null>(null);

  const mutation = useUploadEvidence({
    onSuccess: (data) => {
      setResult(data);
      setProgress(100);
    },
  });

  const updateField = useCallback(
    <K extends keyof EvidenceUploadForm>(key: K, value: EvidenceUploadForm[K]) => {
      setForm((prev) => ({ ...prev, [key]: value }));
    },
    [],
  );

  const submit = useCallback(() => {
    if (!form.file || !form.indicatorId || !form.criterionId || !form.description.trim()) {
      return;
    }
    setProgress(0);
    setResult(null);
    mutation.mutate({
      data: {
        indicatorId: form.indicatorId,
        criterionId: form.criterionId,
        description: form.description,
        file: form.file,
      },
      onProgress: setProgress,
    });
  }, [form, mutation]);

  const isLargeFile = form.file !== null && form.file.size > LARGE_FILE_THRESHOLD_BYTES;
  const isBlocked = mutation.isPending;

  return {
    form,
    updateField,
    submit,
    progress,
    isLargeFile,
    isBlocked,
    result,
    error: mutation.error,
    isSubmitting: mutation.isPending,
  };
}
