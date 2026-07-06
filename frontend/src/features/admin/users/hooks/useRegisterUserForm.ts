import { useState } from 'react';
import { useRegister } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type { RegisterUserResponse } from '../../../../api/model';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import {
  ASSIGNABLE_ROLES,
  ROLE_LABELS,
  ROLE_REQUIRES_PROGRAM,
  type BackendRoleCode,
} from '../../../../lib/auth/roleLabels';
import { UMSS_EMAIL_PATTERN } from '../../../../lib/auth/types';

interface RegisterUserFormState {
  email: string;
  role: BackendRoleCode;
}

export function useRegisterUserForm() {
  const [form, setForm] = useState<RegisterUserFormState>({
    email: '',
    role: 'TD',
  });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<'email' | 'role' | 'programId', string>>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { mutate, isPending, reset } = useRegister({
    mutation: {
      onSuccess: (response) => {
        const payload = response.data as RegisterUserResponse;
        setSuccessMessage(
          `Usuario registrado (${payload.userId ?? '—'}) con estado inicial ${payload.status ?? 'INACTIVE'}. La contraseña temporal se entrega por canal offline.`,
        );
        setSubmitError(null);
        setForm({ email: '', role: 'TD' });
        setFieldErrors({});
      },
      onError: (error) => {
        setSubmitError(getApiErrorMessage(error));
        setSuccessMessage(null);
      },
    },
  });

  const requiresProgram = ROLE_REQUIRES_PROGRAM[form.role];

  const validate = (): boolean => {
    const nextErrors: Partial<Record<'email' | 'role' | 'programId', string>> = {};

    if (!form.email.trim()) {
      nextErrors.email = 'El correo es obligatorio.';
    } else if (!UMSS_EMAIL_PATTERN.test(form.email.trim())) {
      nextErrors.email = 'Solo se permiten correos institucionales @umss.edu.bo.';
    }

    if (requiresProgram) {
      nextErrors.programId =
        'La asignación de carrera requiere el catálogo de programas (pendiente GET /api/v1/programs).';
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

    if (requiresProgram) {
      return;
    }

    mutate({
      data: {
        email: form.email.trim().toLowerCase(),
        role: form.role,
      },
    });
  };

  const setEmail = (email: string) => {
    setForm((current) => ({ ...current, email }));
    reset();
  };

  const setRole = (role: BackendRoleCode) => {
    setForm((current) => ({ ...current, role }));
    setFieldErrors((current) => ({ ...current, programId: undefined }));
    reset();
  };

  const roleOptions = ASSIGNABLE_ROLES.map((role) => ({
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
    setEmail,
    setRole,
    handleSubmit,
  };
}
