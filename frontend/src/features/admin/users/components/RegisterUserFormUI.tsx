import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { Select } from '../../../../components/ui/Select';
import { TextInput } from '../../../../components/ui/TextInput';
import type { BackendRoleCode } from '../../../../lib/auth/roleLabels';
import { UMSS_EMAIL_SUFFIX } from '../../../../lib/auth/types';
import type { ChangeEvent } from 'react';

interface RoleOption {
  value: BackendRoleCode;
  label: string;
}

interface RegisterUserFormUIProps {
  email: string;
  role: BackendRoleCode;
  roleOptions: RoleOption[];
  requiresProgram: boolean;
  emailError?: string;
  programError?: string;
  submitError?: string | null;
  successMessage?: string | null;
  isSubmitting: boolean;
  onEmailChange: (value: string) => void;
  onRoleChange: (value: BackendRoleCode) => void;
  onSubmit: () => void;
}

export function RegisterUserFormUI({
  email,
  role,
  roleOptions,
  requiresProgram,
  emailError,
  programError,
  submitError,
  successMessage,
  isSubmitting,
  onEmailChange,
  onRoleChange,
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
              value=""
              disabled
              options={[{ value: '', label: 'Pendiente — GET /api/v1/programs' }]}
              error={programError}
              helperText="El catálogo de programas se habilitará cuando el backend exponga GET /api/v1/programs."
            />
          </div>
        )}

        <div className="md:col-span-2">
          <Button type="submit" isLoading={isSubmitting} disabled={requiresProgram}>
            Registrar usuario
          </Button>
        </div>
      </form>
    </section>
  );
}
