import { ArrowLeft, Eye, EyeOff, Lock, LogIn, Mail } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';
import { Alert } from '../../../components/ui/Alert';
import { UMSS_EMAIL_SUFFIX } from '../../../lib/auth/types';

interface QuickAccessPreset {
  id: string;
  label: string;
  email: string;
}

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
  onQuickAccess: (email: string) => void;
  onBack?: () => void;
}

const QUICK_ACCESS_PRESETS: QuickAccessPreset[] = [
  { id: 'admin', label: 'Admin', email: `admin${UMSS_EMAIL_SUFFIX}` },
  { id: 'tecnico', label: 'Técnico', email: `tecnico${UMSS_EMAIL_SUFFIX}` },
  { id: 'coord-ceub', label: 'Coordinador CEUB', email: `coord.ceub${UMSS_EMAIL_SUFFIX}` },
  { id: 'coord-arcusur', label: 'Coordinador ARCU-SUR', email: `coord.arcusur${UMSS_EMAIL_SUFFIX}` },
];

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
  onQuickAccess,
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

        <div className="mt-10">
          <div className="mb-4 flex items-center gap-4">
            <div className="h-px flex-1 bg-gray-200" />
            <span className="text-label-md font-medium tracking-wide text-gray-500">ACCESO RÁPIDO</span>
            <div className="h-px flex-1 bg-gray-200" />
          </div>

          <div className="grid grid-cols-2 gap-3">
            {QUICK_ACCESS_PRESETS.map((preset) => (
              <button
                key={preset.id}
                type="button"
                onClick={() => onQuickAccess(preset.email)}
                className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-3 text-left transition-colors hover:border-primary-300 hover:bg-primary-50"
              >
                <span className="block text-label-md font-medium text-primary-800">{preset.label}</span>
                <span className="mt-1 block truncate text-label-md text-gray-500">{preset.email}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
