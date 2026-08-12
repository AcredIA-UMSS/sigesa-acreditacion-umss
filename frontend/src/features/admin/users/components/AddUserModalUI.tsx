import { Eye, EyeOff, X } from 'lucide-react';
import type { ChangeEvent } from 'react';
import { useState } from 'react';
import { Button } from '../../../../components/ui/Button';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
import type { BackendRoleCode } from '../../../../lib/auth/roleLabels';
import type { UserFormErrors } from '../lib/userFormValidation';

interface RoleOption {
  value: BackendRoleCode;
  label: string;
}

interface ProgramOption {
  value: string;
  label: string;
}

export interface AddUserFormViewModel {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: BackendRoleCode | '';
  programId: string;
  password: string;
  confirmPassword: string;
}

interface AddUserModalUIProps {
  isOpen: boolean;
  form: AddUserFormViewModel;
  fieldErrors: UserFormErrors;
  submitError?: string | null;
  roleOptions: RoleOption[];
  programOptions: ProgramOption[];
  requiresProgram: boolean;
  isProgramsLoading: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: () => void;
  onFirstNameChange: (value: string) => void;
  onLastNameChange: (value: string) => void;
  onEmailChange: (value: string) => void;
  onPhoneChange: (value: string) => void;
  onRoleChange: (value: BackendRoleCode | '') => void;
  onProgramChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onConfirmPasswordChange: (value: string) => void;
}

function SectionDivider({ title }: { title: string }) {
  return (
    <div className="relative my-6">
      <div className="absolute inset-0 flex items-center">
        <div className="w-full border-t border-gray-300" />
      </div>
      <div className="relative flex justify-center">
        <span className="bg-body px-4 text-body-md font-medium text-gray-700">{title}</span>
      </div>
    </div>
  );
}

export function AddUserModalUI({
  isOpen,
  form,
  fieldErrors,
  submitError,
  roleOptions,
  programOptions,
  requiresProgram,
  isProgramsLoading,
  isSubmitting,
  onClose,
  onSubmit,
  onFirstNameChange,
  onLastNameChange,
  onEmailChange,
  onPhoneChange,
  onRoleChange,
  onProgramChange,
  onPasswordChange,
  onConfirmPasswordChange,
}: AddUserModalUIProps) {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar modal"
        onClick={onClose}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="add-user-title"
        className="relative z-10 w-full max-w-3xl rounded-2xl border border-gray-200 bg-body shadow-2xl"
      >
        <div className="flex items-center justify-between border-b border-gray-100 px-8 py-5">
          <h2 id="add-user-title" className="text-heading-lg font-bold uppercase tracking-wide text-gray-900">
            Añadir usuario
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-primary-700"
            aria-label="Cerrar"
          >
            <X size={22} />
          </button>
        </div>

        <form
          className="max-h-[75vh] overflow-y-auto px-8 py-6"
          onSubmit={(event) => {
            event.preventDefault();
            onSubmit();
          }}
        >
          <SectionDivider title="Datos personales" />

          {submitError && (
            <div className="mb-4 rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
              {submitError}
            </div>
          )}

          <div className="grid gap-5 md:grid-cols-2">
            <TextInput
              label="Nombre(s)"
              requiredMark
              value={form.firstName}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onFirstNameChange(event.target.value)}
              error={fieldErrors.firstName}
              autoComplete="given-name"
            />
            <TextInput
              label="Apellido(s)"
              requiredMark
              value={form.lastName}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onLastNameChange(event.target.value)}
              error={fieldErrors.lastName}
              autoComplete="family-name"
            />
            <TextInput
              label="Correo electrónico"
              requiredMark
              type="email"
              placeholder="nombre@umss.edu.bo"
              value={form.email}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onEmailChange(event.target.value)}
              error={fieldErrors.email}
              autoComplete="email"
            />
            <TextInput
              label="Celular"
              requiredMark
              inputMode="numeric"
              placeholder="71234567"
              value={form.phoneNumber}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onPhoneChange(event.target.value)}
              error={fieldErrors.phoneNumber}
              autoComplete="tel"
            />
            <Select
              label="Rol"
              requiredMark
              value={form.role}
              onChange={(event: ChangeEvent<HTMLSelectElement>) =>
                onRoleChange(event.target.value as BackendRoleCode | '')
              }
              options={[
                { value: '', label: 'Seleccione' },
                ...roleOptions.map((option) => ({ value: option.value, label: option.label })),
              ]}
              error={fieldErrors.role}
            />
            {requiresProgram && (
              <Select
                label="Carrera asignada"
                requiredMark
                value={form.programId}
                disabled={isProgramsLoading || programOptions.length === 0}
                onChange={(event: ChangeEvent<HTMLSelectElement>) => onProgramChange(event.target.value)}
                options={[
                  {
                    value: '',
                    label: isProgramsLoading ? 'Cargando carreras…' : 'Seleccione una carrera',
                  },
                  ...programOptions,
                ]}
                error={fieldErrors.programId}
              />
            )}
            <TextInput
              label="Contraseña"
              requiredMark
              type={showPassword ? 'text' : 'password'}
              value={form.password}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onPasswordChange(event.target.value)}
              error={fieldErrors.password}
              autoComplete="new-password"
              suffixIcon={
                <button
                  type="button"
                  tabIndex={-1}
                  className="text-gray-500 hover:text-primary-600"
                  onClick={() => setShowPassword((current) => !current)}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              }
            />
            <TextInput
              label="Repetir contraseña"
              requiredMark
              type={showConfirmPassword ? 'text' : 'password'}
              value={form.confirmPassword}
              onChange={(event: ChangeEvent<HTMLInputElement>) =>
                onConfirmPasswordChange(event.target.value)
              }
              error={fieldErrors.confirmPassword}
              autoComplete="new-password"
              suffixIcon={
                <button
                  type="button"
                  tabIndex={-1}
                  className="text-gray-500 hover:text-primary-600"
                  onClick={() => setShowConfirmPassword((current) => !current)}
                  aria-label={showConfirmPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              }
            />
          </div>

          <div className="mt-8 flex flex-col-reverse gap-3 border-t border-gray-100 pt-6 sm:flex-row sm:justify-end">
            <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting}>
              Cerrar
            </Button>
            <Button type="submit" isLoading={isSubmitting}>
              Guardar
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
