import type { ChangeEvent } from 'react';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
import type { BackendRoleCode } from '../../../../lib/auth/roleLabels';
import { UMSS_EMAIL_SUFFIX } from '../../../../lib/auth/types';

interface RoleOption {
  value: BackendRoleCode;
  label: string;
}

interface ProgramOption {
  value: string;
  label: string;
}

interface RegisterUserFormUIProps {
  email: string;
  role: BackendRoleCode;
  programId: string;
  roleOptions: RoleOption[];
  programOptions: ProgramOption[];
  requiresProgram: boolean;
  isProgramsLoading: boolean;
  emailError?: string;
  programError?: string;
  submitError?: string | null;
  successMessage?: string | null;
  isSubmitting: boolean;
  onEmailChange: (value: string) => void;
  onRoleChange: (value: BackendRoleCode) => void;
  onProgramChange: (value: string) => void;
  onSubmit: () => void;
}

export function RegisterUserFormUI({
  email,
  role,
  programId,
  roleOptions,
  programOptions,
  requiresProgram,
  isProgramsLoading,
  emailError,
  programError,
  submitError,
  successMessage,
  isSubmitting,
  onEmailChange,
  onRoleChange,
  onProgramChange,
  onSubmit,
}: RegisterUserFormUIProps) {
  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6">
        <h2 className="text-heading-md text-primary-800">Alta de usuario</h2>
        <p className="mt-1 text-body-md text-gray-600">
          Registra cuentas internas con estado inicial INACTIVE. La contraseña temporal no se muestra
          en pantalla.
        </p>
      </div>

      {successMessage && (
        <div className="mb-4">
          <Alert variant="success">{successMessage}</Alert>
        </div>
      )}

      {submitError && (
        <div className="mb-4">
          <Alert variant="error">{submitError}</Alert>
        </div>
      )}

      <form
        className="grid gap-4 md:grid-cols-2"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit();
        }}
      >
        <TextInput
          label="Correo institucional"
          type="email"
          placeholder={`nombre${UMSS_EMAIL_SUFFIX}`}
          value={email}
          onChange={(event: ChangeEvent<HTMLInputElement>) => onEmailChange(event.target.value)}
          error={emailError}
        />

        <Select
          label="Rol"
          value={role}
          onChange={(event: ChangeEvent<HTMLSelectElement>) =>
            onRoleChange(event.target.value as BackendRoleCode)
          }
          options={roleOptions.map((option) => ({ value: option.value, label: option.label }))}
        />

        {requiresProgram && (
          <div className="md:col-span-2">
            <Select
              label="Carrera (programId)"
              value={programId}
              disabled={isProgramsLoading || programOptions.length === 0}
              onChange={(event: ChangeEvent<HTMLSelectElement>) => onProgramChange(event.target.value)}
              options={[
                { value: '', label: isProgramsLoading ? 'Cargando carreras…' : 'Seleccione una carrera' },
                ...programOptions,
              ]}
              error={programError}
            />
          </div>
        )}

        <div className="md:col-span-2">
          <Button type="submit" isLoading={isSubmitting}>
            Registrar usuario
          </Button>
        </div>
      </form>
    </section>
  );
}
