import { ArrowLeft, Eye, EyeOff, Lock, LogIn, Mail } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';
import { Alert } from '../../../components/ui/Alert';
import { UMSS_EMAIL_SUFFIX } from '../../../lib/auth/types';

interface LoginFormUIProps {
  email: string;
  password: string;
  emailError?: string;
  passwordError?: string;
  submitError?: string | null;
  isSubmitting: boolean;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onSubmit: () => void;
  onBack?: () => void;
}

export function LoginFormUI({
  email,
  password,
  emailError,
  passwordError,
  submitError,
  isSubmitting,
  onEmailChange,
  onPasswordChange,
  onSubmit,
  onBack,
}: LoginFormUIProps) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="flex min-h-screen flex-col bg-body px-8 py-10 lg:px-16">
      {onBack && (
        <button
          type="button"
          onClick={onBack}
          className="mb-8 inline-flex items-center gap-2 text-body-md text-gray-600 hover:text-primary-600"
        >
          <ArrowLeft size={18} />
          Atrás
        </button>
      )}

      <div className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center">
        <div className="mb-8">
          <h1 className="text-heading-xl text-primary-800">Bienvenido</h1>
          <p className="mt-2 text-body-md text-gray-600">Ingresa tus credenciales para continuar</p>
        </div>

        {submitError && (
          <div className="mb-6">
            <Alert variant="error">{submitError}</Alert>
          </div>
        )}

        <form
          className="space-y-5"
          onSubmit={(event) => {
            event.preventDefault();
            onSubmit();
          }}
        >
          <TextInput
            label="Correo Institucional"
            type="email"
            autoComplete="username"
            placeholder={`nombre${UMSS_EMAIL_SUFFIX}`}
            value={email}
            onChange={(event) => onEmailChange(event.target.value)}
            error={emailError}
            prefixIcon={<Mail size={18} />}
          />

          <TextInput
            label="Contraseña"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            value={password}
            onChange={(event) => onPasswordChange(event.target.value)}
            error={passwordError}
            prefixIcon={<Lock size={18} />}
            suffixIcon={
              <button
                type="button"
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                onClick={() => setShowPassword((current) => !current)}
                className="hover:text-primary-600"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            }
          />

          <p className="text-center">
            <span className="text-body-md text-secondary hover:underline">
              ¿Olvidaste tu contraseña?
            </span>
          </p>

          <Button type="submit" className="w-full" isLoading={isSubmitting}>
            Iniciar sesión
            <LogIn size={18} />
          </Button>
        </form>
      </div>
    </div>
  );
}
