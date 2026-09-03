import { useState } from 'react';
import { Bell, Settings } from 'lucide-react';
import { Sidebar } from '../../../../components/layout/Sidebar';
import { useAuth } from '../../../../lib/auth/useAuth';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import { AddUserModalUI } from '../components/AddUserModalUI';
import { UserSaveSuccessDialog } from '../components/UserSaveSuccessDialog';
import { UsersCopilotPanel } from '../components/UsersCopilotPanel';
import { UsersTableUI } from '../components/UsersTableUI';
import { useDeactivateUserAction } from '../hooks/useDeactivateUserAction';
import { useRegisterUserForm } from '../hooks/useRegisterUserForm';
import { useUsersList } from '../hooks/useUsersList';

export function UsersAdminPage() {
  const { session } = useAuth();
  const registerForm = useRegisterUserForm();
  const usersList = useUsersList();
  const deactivateAction = useDeactivateUserAction();
  const [actionError, setActionError] = useState<string | null>(null);

  // Solo montar copiloto para JD (defensa en profundidad; ruta ya es JdOnlyRoute).
  const showUsersCopilot = session?.role === 'JD';

  const handleDeactivate = async (userId: string) => {
    setActionError(null);
    const result = await deactivateAction.deactivateUser(userId);
    if (!result.ok) {
      setActionError(result.message);
    }
  };

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

        <main data-app-scroll className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-7xl space-y-6">
            <div>
              <div className="mb-4 h-1 w-12 bg-secondary" />
              <h1 className="text-heading-xl text-primary-800">Gestión de usuarios</h1>
              <p className="mt-2 text-body-lg text-gray-600">
                Administre cuentas internas, asigne roles y comparta credenciales al momento del alta.
              </p>
            </div>

            {actionError && (
              <div className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
                {actionError}
              </div>
            )}

            <UsersTableUI
              users={usersList.users}
              isLoading={usersList.isLoading}
              isError={usersList.isError}
              errorMessage={usersList.error ? getApiErrorMessage(usersList.error) : undefined}
              isDeactivating={deactivateAction.isDeactivating}
              deactivatingUserId={deactivateAction.deactivatingUserId}
              onAddUser={registerForm.openModal}
              onDeactivate={handleDeactivate}
            />

            {showUsersCopilot ? <UsersCopilotPanel /> : null}
          </div>
        </main>
      </div>

      <AddUserModalUI
        isOpen={registerForm.isModalOpen}
        form={registerForm.form}
        fieldErrors={registerForm.fieldErrors}
        submitError={registerForm.submitError}
        roleOptions={registerForm.roleOptions}
        programOptions={registerForm.programOptions}
        requiresProgram={registerForm.requiresProgram}
        isProgramsLoading={registerForm.isProgramsLoading}
        isSubmitting={registerForm.isPending}
        onClose={registerForm.closeModal}
        onSubmit={registerForm.handleSubmit}
        onFirstNameChange={registerForm.setFirstName}
        onLastNameChange={registerForm.setLastName}
        onEmailChange={registerForm.setEmail}
        onPhoneChange={registerForm.setPhoneNumber}
        onRoleChange={registerForm.setRole}
        onProgramChange={registerForm.setProgramId}
        onPasswordChange={registerForm.setPassword}
        onConfirmPasswordChange={registerForm.setConfirmPassword}
      />

      <UserSaveSuccessDialog
        isOpen={registerForm.saveSuccess !== null}
        fullName={registerForm.saveSuccess?.fullName ?? ''}
        email={registerForm.saveSuccess?.email ?? ''}
        password={registerForm.saveSuccess?.password ?? ''}
        onClose={registerForm.closeSuccessDialog}
      />
    </div>
  );
}
