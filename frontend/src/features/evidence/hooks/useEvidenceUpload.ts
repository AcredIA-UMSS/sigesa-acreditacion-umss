import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useUpload } from '../../../api/endpoints/evidence/evidence';
import type { UploadEvidenceResponse } from '../../../api/model';
import { mapUploadError } from './mapUploadError';

/** 10 MiB — aviso de archivo grande en UI */
export const LARGE_FILE_THRESHOLD_BYTES = 10 * 1024 * 1024;

/** UUIDs seed de EvidenceDataLoader (demo local UC-004). */
export const SEED_INDICATOR_ID = '550e8400-e29b-41d4-a716-446655440003';
export const SEED_CRITERION_ID = '550e8400-e29b-41d4-a716-446655440002';

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
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState<EvidenceUploadForm>(defaultForm);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<UploadEvidenceResponse | null>(null);
  const [validationErrors, setValidationErrors] =
    useState<EvidenceUploadValidationErrors>({});

  useEffect(() => {
    const indicatorId = searchParams.get('indicatorId')?.trim() ?? '';
    const criterionId = searchParams.get('criterionId')?.trim() ?? '';
    if (!indicatorId && !criterionId) {
      return;
    }
    setForm((prev) => ({
      ...prev,
      indicatorId: indicatorId || prev.indicatorId,
      criterionId: criterionId || prev.criterionId,
    }));
  }, [searchParams]);

  const mutation = useUpload({
    mutation: {
      onSuccess: (response) => {
        if (response.status === 201) {
          setResult(response.data);
        }
        setProgress(100);
        setValidationErrors({});
      },
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
      indicatorId: form.indicatorId.trim(),
      data: {
        file: form.file,
        criterionId: form.criterionId.trim(),
        description: form.description.trim(),
      },
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
    errorMessage: mapUploadError(
      (mutation.error as any) instanceof Error ? (mutation.error as any) : null,
    ),
    isSubmitting: mutation.isPending,
  };
}
