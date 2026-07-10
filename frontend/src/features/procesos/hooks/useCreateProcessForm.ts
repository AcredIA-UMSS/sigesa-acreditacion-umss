import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateProcess } from '../../../api/endpoints/accreditation-process-controller/accreditation-process-controller';
import { useList1 } from '../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import {
  isTemplateValidated,
  mapTemplateToOption,
  useListTemplates,
} from '../../../api/endpoints/template-controller/template-controller';
import type { CreateProcessRequestType, ProcessResponse, ProgramSummaryResponse } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useAuth } from '../../../lib/auth/useAuth';

interface CreateProcessFormState {
  careerId: string;
  templateId: string;
  period: string;
}

interface TemplateOption {
  id: string;
  label: string;
  type: CreateProcessRequestType;
  taxonomyVersion: string;
  activePeriod?: string;
}

export function useCreateProcessForm() {
  const navigate = useNavigate();
  const { session } = useAuth();
  const programsQuery = useList1();
  const templatesQuery = useListTemplates({
    query: { enabled: session?.role === 'JD' },
  });
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
    .filter((template) => isTemplateValidated(template) && template.id && template.type)
    .map((template) => {
      const mapped = mapTemplateToOption(template);
      return {
        id: mapped.id,
        label: mapped.label,
        type: mapped.type as CreateProcessRequestType,
        taxonomyVersion: mapped.taxonomyVersion,
        activePeriod: mapped.activePeriod,
      };
    });

  const periodOptions = Array.from(
    new Set(
      templateOptions
        .map((template) => template.activePeriod)
        .filter((period): period is string => Boolean(period)),
    ),
  ).map((period) => ({ value: period, label: period }));

  const fallbackPeriods = ['2026-2', '2026-1', '2025-2'].map((period) => ({
    value: period,
    label: period,
  }));

  const selectedTemplate = templateOptions.find((template) => template.id === form.templateId);

  const templatesErrorMessage = templatesQuery.isError
    ? getApiErrorMessage(
        templatesQuery.error,
        'No se pudo cargar plantillas. Verifique que el backend esté activo y que inició sesión como JD.',
      )
    : null;

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

    if (templatesQuery.isError) {
      setSubmitError(templatesErrorMessage ?? 'No se pudieron cargar las plantillas.');
      return;
    }

    if (templateOptions.length === 0) {
      setSubmitError(
        'No hay plantillas validadas disponibles. Reinicie el backend (seed dev) o active una plantilla.',
      );
      return;
    }

    if (!validate()) {
      return;
    }

    if (!selectedTemplate) {
      setSubmitError('La plantilla seleccionada no es válida. Vuelva a elegirla en el listado.');
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
    const template = templateOptions.find((item) => item.id === templateId);
    setForm((current) => ({
      ...current,
      templateId,
      period: template?.activePeriod ?? current.period,
    }));
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
    templatesErrorMessage,
    templatesEmpty: !templatesQuery.isLoading && !templatesQuery.isError && templateOptions.length === 0,
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
