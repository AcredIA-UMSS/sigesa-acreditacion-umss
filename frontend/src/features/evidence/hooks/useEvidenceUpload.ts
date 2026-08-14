import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useUpload } from '../../../api/endpoints/evidence-controller/evidence-controller';
import type { UploadEvidenceResponse } from '../../../api/model';
import { mapUploadError } from './mapUploadError';

/** 10 MiB — aviso de archivo grande en UI */
export const LARGE_FILE_THRESHOLD_BYTES = 10 * 1024 * 1024;

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

const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function isUuid(value: string): boolean {
  return UUID_RE.test(value.trim());
}

function validateForm(form: EvidenceUploadForm): EvidenceUploadValidationErrors {
  const errors: EvidenceUploadValidationErrors = {};

  const indicatorId = form.indicatorId.trim();
  const criterionId = form.criterionId.trim();

  if (!indicatorId) {
    errors.indicatorId = 'Seleccione un indicador.';
  } else if (!isUuid(indicatorId)) {
    errors.indicatorId = 'El indicador seleccionado no es válido.';
  }

  if (!criterionId) {
    errors.criterionId = 'Seleccione un indicador para fijar el criterio.';
  } else if (!isUuid(criterionId)) {
    errors.criterionId = 'El criterio asociado no es válido.';
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
        if (response.status === 200) {
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

  /** Al elegir indicador en el select, fija indicatorId + criterionId (1:1). */
  const selectIndicator = useCallback(
    (indicatorId: string, criterionId: string) => {
      setForm((prev) => ({
        ...prev,
        indicatorId,
        criterionId,
      }));
      setValidationErrors((prev) => {
        if (!prev.indicatorId && !prev.criterionId) return prev;
        const next = { ...prev };
        delete next.indicatorId;
        delete next.criterionId;
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
      params: {
        criterionId: form.criterionId.trim(),
        description: form.description.trim(),
      },
      data: { file: form.file },
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
    selectIndicator,
    submit,
    reset,
    progress,
    isLargeFile,
    isBlocked,
    result,
    validationErrors,
    errorMessage: mapUploadError(
      mutation.error instanceof Error ? mutation.error : null,
    ),
    isSubmitting: mutation.isPending,
  };
}
