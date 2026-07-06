import { useQueryClient } from '@tanstack/react-query';
import { useRegister } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { getListQueryKey } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { useList1 } from '../../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import type { RegisterUserResponse, ProgramSummaryResponse } from '../../../../api/model';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import {
  ASSIGNABLE_ROLES,
  ROLE_LABELS,
  ROLE_REQUIRES_PROGRAM,
  type BackendRoleCode,
} from '../../../../lib/auth/roleLabels';
import { UMSS_EMAIL_PATTERN } from '../../../../lib/auth/types';
import { useState } from 'react';

interface RegisterUserFormState {
  email: string;
  role: BackendRoleCode;
  programId: string;
}

export function useRegisterUserForm() {
  const queryClient = useQueryClient();
  const programsQuery = useList1();
  const [form, setForm] = useState<RegisterUserFormState>({
    email: '',
    role: 'TD',
    programId: '',
  });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<'email' | 'role' | 'programId', string>>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { mutate, isPending, reset } = useRegister({
    mutation: {
      onSuccess: async (response) => {
        const payload = response.data as RegisterUserResponse;
        setSuccessMessage(
          `Usuario registrado (${payload.userId ?? '—'}) con estado inicial ${payload.status ?? 'INACTIVE'}. La contraseña temporal se entrega por canal offline.`,
        );
        setSubmitError(null);
        setForm({ email: '', role: 'TD', programId: '' });
        setFieldErrors({});
        await queryClient.invalidateQueries({ queryKey: getListQueryKey(undefined) });
      },
      onError: (error) => {
        setSubmitError(getApiErrorMessage(error));
        setSuccessMessage(null);
      },
    },
  });

  const requiresProgram = ROLE_REQUIRES_PROGRAM[form.role];
  const programOptions = (programsQuery.data?.data ?? [])
    .filter((program: ProgramSummaryResponse) => program.id && program.name)
    .map((program: ProgramSummaryResponse) => ({
      value: program.id as string,
      label: program.code ? `${program.code} — ${program.name}` : (program.name as string),
    }));

  const validate = (): boolean => {
    const nextErrors: Partial<Record<'email' | 'role' | 'programId', string>> = {};

    if (!form.email.trim()) {
      nextErrors.email = 'El correo es obligatorio.';
    } else if (!UMSS_EMAIL_PATTERN.test(form.email.trim())) {
      nextErrors.email = 'Solo se permiten correos institucionales @umss.edu.bo.';
    }

    if (requiresProgram && !form.programId) {
      nextErrors.programId = 'Seleccione una carrera para el rol Coordinador.';
    }

    setFieldErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = () => {
    setSubmitError(null);
    setSuccessMessage(null);

    if (!validate()) {
      return;
    }

    mutate({
      data: {
        email: form.email.trim().toLowerCase(),
        role: form.role,
        ...(requiresProgram ? { programId: form.programId } : {}),
      },
    });
  };

  const setEmail = (email: string) => {
    setForm((current) => ({ ...current, email }));
    reset();
  };

  const setRole = (role: BackendRoleCode) => {
    setForm((current) => ({ ...current, role, programId: '' }));
    setFieldErrors((current) => ({ ...current, programId: undefined }));
    reset();
  };

  const setProgramId = (programId: string) => {
    setForm((current) => ({ ...current, programId }));
    setFieldErrors((current) => ({ ...current, programId: undefined }));
    reset();
  };

  const roleOptions = ASSIGNABLE_ROLES.map((role: BackendRoleCode) => ({
    value: role,
    label: ROLE_LABELS[role],
  }));

  return {
    form,
    fieldErrors,
    submitError,
    successMessage,
    isPending,
    requiresProgram,
    roleOptions,
    programOptions,
    isProgramsLoading: programsQuery.isLoading,
    setEmail,
    setRole,
    setProgramId,
    handleSubmit,
  };
}
