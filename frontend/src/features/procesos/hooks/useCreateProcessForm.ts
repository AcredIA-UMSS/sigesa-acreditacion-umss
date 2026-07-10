import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateProcess } from '../../../api/endpoints/accreditation-process-controller/accreditation-process-controller';
import { useList1 } from '../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import { useListTemplates } from '../../../api/endpoints/template-controller/template-controller';
import type { ProcessResponse, ProgramSummaryResponse, TemplateSummaryResponse } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

interface CreateProcessFormState {
  careerId: string;
  templateId: string;
  period: string;
}

interface TemplateOption {
  id: string;
  label: string;
  type: NonNullable<TemplateSummaryResponse['type']>;
  taxonomyVersion: string;
}

export function useCreateProcessForm() {
  const navigate = useNavigate();
  const programsQuery = useList1();
  const templatesQuery = useListTemplates();
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
          navigate('/procesos');
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

  const templateOptions: TemplateOption[] = (templatesQuery.data?.data ?? [])
    .filter((template) => template.validated && template.id && template.type)
    .map((template) => ({
      id: template.id as string,
      label: `${template.type} (${template.taxonomyVersion ?? 'sin versión'})`,
      type: template.type as NonNullable<TemplateSummaryResponse['type']>,
      taxonomyVersion: template.taxonomyVersion ?? '',
    }));

  const periodOptions = Array.from(
    new Set(
      (templatesQuery.data?.data ?? [])
        .map((template) => template.activePeriod)
        .filter((period): period is string => Boolean(period)),
    ),
  ).map((period) => ({ value: period, label: period }));

  const fallbackPeriods = ['2026-1', '2025-2', '2026-2'].map((period) => ({
    value: period,
    label: period,
  }));

  const selectedTemplate = templateOptions.find((template) => template.id === form.templateId);

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
    navigate('/procesos');
  };

  return {
    form,
    fieldErrors,
    submitError,
    successMessage,
    isPending,
    isProgramsLoading: programsQuery.isLoading,
    isProgramsError: programsQuery.isError,
    isTemplatesLoading: templatesQuery.isLoading,
    isTemplatesError: templatesQuery.isError,
    programOptions,
    templateOptions: templateOptions.map((template) => ({
      value: template.id,
      label: template.label,
    })),
    periodOptions: periodOptions.length > 0 ? periodOptions : fallbackPeriods,
    selectedTemplate,
    setCareerId,
    setTemplateId,
    setPeriod,
    handleSubmit,
    handleCancel,
  };
}
