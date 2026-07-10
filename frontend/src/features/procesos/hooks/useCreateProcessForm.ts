import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateProcess } from '../../../api/endpoints/accreditation-process-controller/accreditation-process-controller';
import { useList1 } from '../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import type { ProcessResponse, ProgramSummaryResponse } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { SEED_PERIODS, SEED_VALIDATED_TEMPLATES } from '../constants/devSeedCatalog';

interface CreateProcessFormState {
  careerId: string;
  templateId: string;
  period: string;
}

export function useCreateProcessForm() {
  const navigate = useNavigate();
  const programsQuery = useList1();
  const [form, setForm] = useState<CreateProcessFormState>({
    careerId: '',
    templateId: '',
    period: '',
  });
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<'careerId' | 'templateId' | 'period', string>>
  >({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { mutate, isPending, reset } = useCreateProcess({
    mutation: {
      onSuccess: (response) => {
        const payload = response.data as ProcessResponse;
        setSuccessMessage(
          `Proceso creado (${payload.processId ?? '—'}) con estado ${payload.status ?? 'ACTIVE'}.`,
        );
        setSubmitError(null);
        setFieldErrors({});
        setTimeout(() => {
          navigate('/dashboard');
        }, 2000);
      },
      onError: (error) => {
        setSubmitError(getApiErrorMessage(error));
        setSuccessMessage(null);
      },
    },
  });

  const programOptions = (programsQuery.data?.data ?? [])
    .filter((program: ProgramSummaryResponse) => program.id && program.name)
    .map((program: ProgramSummaryResponse) => ({
      value: program.id as string,
      label: program.code ? `${program.code} — ${program.name}` : (program.name as string),
    }));

  const templateOptions = SEED_VALIDATED_TEMPLATES.map((template) => ({
    value: template.id,
    label: template.label,
  }));

  const periodOptions = SEED_PERIODS.map((period) => ({
    value: period,
    label: period,
  }));

  const selectedTemplate = SEED_VALIDATED_TEMPLATES.find((t) => t.id === form.templateId);

  const validate = (): boolean => {
    const nextErrors: Partial<Record<'careerId' | 'templateId' | 'period', string>> = {};

    if (!form.careerId) {
      nextErrors.careerId = 'Seleccione un programa/carrera.';
    }
    if (!form.templateId) {
      nextErrors.templateId = 'Seleccione una plantilla normativa.';
    }
    if (!form.period.trim()) {
      nextErrors.period = 'Seleccione el periodo académico.';
    }

    setFieldErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    if (!validate() || !selectedTemplate) {
      return;
    }

    mutate({
      data: {
        templateId: form.templateId,
        careerId: form.careerId,
        period: form.period,
        type: selectedTemplate.type,
      },
    });
  };

  const setCareerId = (careerId: string) => {
    setForm((current) => ({ ...current, careerId }));
    setFieldErrors((current) => ({ ...current, careerId: undefined }));
    reset();
  };

  const setTemplateId = (templateId: string) => {
    setForm((current) => ({ ...current, templateId }));
    setFieldErrors((current) => ({ ...current, templateId: undefined }));
    reset();
  };

  const setPeriod = (period: string) => {
    setForm((current) => ({ ...current, period }));
    setFieldErrors((current) => ({ ...current, period: undefined }));
    reset();
  };

  const handleCancel = () => {
    navigate('/dashboard');
  };

  return {
    form,
    fieldErrors,
    submitError,
    successMessage,
    isPending,
    isProgramsLoading: programsQuery.isLoading,
    isProgramsError: programsQuery.isError,
    programOptions,
    templateOptions,
    periodOptions,
    selectedTemplate,
    setCareerId,
    setTemplateId,
    setPeriod,
    handleSubmit,
    handleCancel,
  };
}
