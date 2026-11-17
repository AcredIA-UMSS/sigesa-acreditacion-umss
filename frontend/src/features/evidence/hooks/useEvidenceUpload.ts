import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { uploadNormativeIndicatorEvidence } from '../api/normativeIndicatorEvidenceApi';
import { mapUploadError } from './mapUploadError';

/** 10 MiB — aviso de archivo grande en UI */
export const LARGE_FILE_THRESHOLD_BYTES = 10 * 1024 * 1024;

export type EvidenceUploadForm = {
  processId: string;
  indicatorId: string;
  description: string;
  file: File | null;
};

export type EvidenceUploadField = keyof EvidenceUploadForm;

export type EvidenceUploadValidationErrors = Partial<
  Record<EvidenceUploadField, string>
>;

export type EvidenceUploadResult = {
  evidenceId: string;
  version: number;
  contentHash: string;
  event?: string;
  currentState: string;
};

const defaultForm: EvidenceUploadForm = {
  processId: '',
  indicatorId: '',
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

  const processId = form.processId.trim();
  const indicatorId = form.indicatorId.trim();

  if (!processId) {
    errors.processId = 'Seleccione un proceso activo.';
  } else if (!isUuid(processId)) {
    errors.processId = 'El proceso seleccionado no es válido.';
  }

  if (!indicatorId) {
    errors.indicatorId = 'Seleccione un indicador.';
  } else if (!isUuid(indicatorId)) {
    errors.indicatorId = 'El indicador seleccionado no es válido.';
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
  const [result, setResult] = useState<EvidenceUploadResult | null>(null);
  const [validationErrors, setValidationErrors] =
    useState<EvidenceUploadValidationErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<Error | null>(null);

  useEffect(() => {
    const processId = searchParams.get('processId')?.trim() ?? '';
    const indicatorId = searchParams.get('indicatorId')?.trim() ?? '';
    if (!processId && !indicatorId) {
      return;
    }
    setForm((prev) => ({
      ...prev,
      processId: processId || prev.processId,
      indicatorId: indicatorId || prev.indicatorId,
    }));
  }, [searchParams]);

  const updateField = useCallback(
    <K extends EvidenceUploadField>(key: K, value: EvidenceUploadForm[K]) => {
      setForm((prev) => {
        const next = { ...prev, [key]: value };
        if (key === 'processId' && value !== prev.processId) {
          next.indicatorId = '';
        }
        return next;
      });
      setValidationErrors((prev) => {
        if (!prev[key]) return prev;
        const next = { ...prev };
        delete next[key];
        if (key === 'processId') {
          delete next.indicatorId;
        }
        return next;
      });
    },
    [],
  );

  const submit = useCallback(async () => {
    const errors = validateForm(form);
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      setResult(null);
      return;
    }

    if (!form.file) return;

    setProgress(10);
    setResult(null);
    setValidationErrors({});
    setSubmitError(null);
    setIsSubmitting(true);

    try {
      setProgress(40);
      const response = await uploadNormativeIndicatorEvidence({
        indicatorId: form.indicatorId.trim(),
        description: form.description.trim(),
        file: form.file,
      });
      setProgress(100);
      setResult({
        evidenceId: response.evidenceId,
        version: response.version,
        contentHash: response.contentHash,
        event: response.event,
        currentState: response.currentState,
      });
    } catch (err) {
      setProgress(0);
      setSubmitError(err instanceof Error ? err : new Error('Error al cargar'));
    } finally {
      setIsSubmitting(false);
    }
  }, [form]);

  const reset = useCallback(() => {
    setForm(defaultForm);
    setProgress(0);
    setResult(null);
    setValidationErrors({});
    setSubmitError(null);
  }, []);

  const isLargeFile =
    form.file !== null && form.file.size > LARGE_FILE_THRESHOLD_BYTES;
  const isBlocked = isSubmitting;

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
    errorMessage: mapUploadError(submitError),
    isSubmitting,
  };
}
