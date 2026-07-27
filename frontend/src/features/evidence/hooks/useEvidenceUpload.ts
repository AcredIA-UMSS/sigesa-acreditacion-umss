import { useCallback, useState } from 'react';
import {
  LARGE_FILE_THRESHOLD_BYTES,
  useUploadEvidence,
} from '../../../api/endpoints/evidence-controller/evidence-controller';
import type { UploadEvidenceResponse } from '../../../api/model';
import { mapUploadError } from './mapUploadError';

export type EvidenceUploadForm = {
  indicatorId: string;
  criterionId: string;
  description: string;
  file: File | null;
};

export type EvidenceUploadField = keyof EvidenceUploadForm;

export type EvidenceUploadValidationErrors = Partial<
  Record<EvidenceUploadField, string>
>;

const defaultForm: EvidenceUploadForm = {
  indicatorId: '',
  criterionId: '',
  description: '',
  file: null,
};

function validateForm(form: EvidenceUploadForm): EvidenceUploadValidationErrors {
  const errors: EvidenceUploadValidationErrors = {};

  if (!form.indicatorId.trim()) {
    errors.indicatorId = 'Indique el identificador del indicador.';
  }
  if (!form.criterionId.trim()) {
    errors.criterionId = 'Indique el identificador del criterio.';
  }
  if (!form.description.trim()) {
    errors.description = 'La descripción es obligatoria.';
  }
  if (!form.file) {
    errors.file = 'Seleccione un archivo para cargar.';
  }

  return errors;
}

export function useEvidenceUpload() {
  const [form, setForm] = useState<EvidenceUploadForm>(defaultForm);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<UploadEvidenceResponse | null>(null);
  const [validationErrors, setValidationErrors] =
    useState<EvidenceUploadValidationErrors>({});

  const mutation = useUploadEvidence({
    onSuccess: (data) => {
      setResult(data);
      setProgress(100);
      setValidationErrors({});
    },
  });

  const updateField = useCallback(
    <K extends EvidenceUploadField>(key: K, value: EvidenceUploadForm[K]) => {
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
      setResult(null);
      return;
    }

    if (!form.file) return;

    setProgress(0);
    setResult(null);
    setValidationErrors({});
    mutation.mutate({
      data: {
        indicatorId: form.indicatorId.trim(),
        criterionId: form.criterionId.trim(),
        description: form.description.trim(),
        file: form.file,
      },
      onProgress: setProgress,
    });
  }, [form, mutation]);

  const reset = useCallback(() => {
    setForm(defaultForm);
    setProgress(0);
    setResult(null);
    setValidationErrors({});
    mutation.reset();
  }, [mutation]);

  const isLargeFile =
    form.file !== null && form.file.size > LARGE_FILE_THRESHOLD_BYTES;
  const isBlocked = mutation.isPending;

  return {
    form,
    updateField,
    submit,
    reset,
    progress,
    isLargeFile,
    isBlocked,
    result,
    validationErrors,
    errorMessage: mapUploadError(mutation.error),
    isSubmitting: mutation.isPending,
  };
}
