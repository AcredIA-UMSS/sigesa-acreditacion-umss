import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useLogin } from '../../../api/endpoints/auth-controller/auth-controller';
import { getLoginErrorMessage } from '../../../lib/api/mapApiError';
import { getPostLoginPath } from '../../../lib/auth/getPostLoginPath';
import { useAuth } from '../../../lib/auth/useAuth';
import { UMSS_EMAIL_PATTERN } from '../../../lib/auth/types';
import type { LoginResponse } from '../../../api/model';

interface LoginFormState {
  email: string;
  password: string;
}

interface LoginLocationState {
  from?: string;
}

export function useLoginForm() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [form, setForm] = useState<LoginFormState>({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<Partial<LoginFormState>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);

  const { mutate, isPending } = useLogin({
    mutation: {
      onSuccess: (response) => {
        const payload = response.data as LoginResponse;
        login(payload);
        const redirectPath =
          (location.state as LoginLocationState | null)?.from ?? getPostLoginPath(payload.role ?? '');
        navigate(redirectPath, { replace: true });
      },
      onError: (error) => {
        setSubmitError(getLoginErrorMessage(error));
      },
    },
  });

  const validate = (): boolean => {
    const nextErrors: Partial<LoginFormState> = {};

    if (!form.email.trim()) {
      nextErrors.email = 'El correo es obligatorio.';
    } else if (!UMSS_EMAIL_PATTERN.test(form.email.trim())) {
      nextErrors.email = 'Solo se permiten correos institucionales @umss.edu.bo.';
    }

    if (!form.password) {
      nextErrors.password = 'La contraseña es obligatoria.';
    }

    setFieldErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = () => {
    setSubmitError(null);
    if (!validate()) {
      return;
    }

    mutate({
      data: {
        email: form.email.trim().toLowerCase(),
        password: form.password,
      },
    });
  };

  const setEmail = (email: string) => {
    setForm((current) => ({ ...current, email }));
    if (fieldErrors.email) {
      setFieldErrors((current) => ({ ...current, email: undefined }));
    }
  };

  const setPassword = (password: string) => {
    setForm((current) => ({ ...current, password }));
    if (fieldErrors.password) {
      setFieldErrors((current) => ({ ...current, password: undefined }));
    }
  };

  const applyQuickAccessEmail = (email: string) => {
    setEmail(email);
    setSubmitError(null);
  };

  return {
    form,
    fieldErrors,
    submitError,
    isPending,
    setEmail,
    setPassword,
    handleSubmit,
    applyQuickAccessEmail,
  };
}
