import { Bell, Settings } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { RegisterUserFormUI } from '../components/RegisterUserFormUI';
import { UsersTablePlaceholderUI } from '../components/UsersTablePlaceholderUI';
import { useRegisterUserForm } from '../hooks/useRegisterUserForm';

export function UsersAdminPage() {
  const registerForm = useRegisterUserForm();

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="users" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div className="text-body-md text-gray-500">
            <span className="text-primary-600">Inicio</span> / Administración de usuarios
          </div>
          <div className="flex items-center gap-4 text-gray-600">
            <button type="button" className="relative hover:text-primary-600" aria-label="Notificaciones">
              <Bell size={24} />
              <span className="absolute right-0 top-0 h-2 w-2 rounded-full bg-secondary" />
            </button>
            <button type="button" className="hover:text-primary-600" aria-label="Configuración">
              <Settings size={24} />
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-6xl space-y-6">
            <div>
              <div className="mb-4 h-1 w-12 bg-secondary" />
              <h1 className="text-heading-xl text-primary-800">Gestión de usuarios</h1>
              <p className="mt-2 text-body-lg text-gray-600">
                Alta de cuentas internas y revocación soft para el rol Jefe (JD).
              </p>
            </div>

            <RegisterUserFormUI
              email={registerForm.form.email}
              role={registerForm.form.role}
              roleOptions={registerForm.roleOptions}
              requiresProgram={registerForm.requiresProgram}
              emailError={registerForm.fieldErrors.email}
              programError={registerForm.fieldErrors.programId}
              submitError={registerForm.submitError}
              successMessage={registerForm.successMessage}
              isSubmitting={registerForm.isPending}
              onEmailChange={registerForm.setEmail}
              onRoleChange={registerForm.setRole}
              onSubmit={registerForm.handleSubmit}
            />

            <UsersTablePlaceholderUI />
          </div>
        </main>
      </div>
    </div>
  );
}
