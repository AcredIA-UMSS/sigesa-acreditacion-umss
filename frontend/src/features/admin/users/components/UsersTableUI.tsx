import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { ConfirmDialog } from '../../../../components/ui/ConfirmDialog';
import type { UserRowViewModel } from '../hooks/useUsersList';

interface UsersTableUIProps {
  users: UserRowViewModel[];
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  isDeactivating: boolean;
  deactivatingUserId: string | null;
  onAddUser: () => void;
  onDeactivate: (userId: string) => void;
}

const STATUS_LABELS: Record<string, string> = {
  INACTIVE: 'Inactivo',
  ACTIVE: 'Activo',
  DEACTIVATED: 'Revocado',
};

const STATUS_STYLES: Record<string, string> = {
  INACTIVE: 'bg-warning/15 text-warning',
  ACTIVE: 'bg-success/15 text-success',
  DEACTIVATED: 'bg-gray-100 text-gray-600',
};

export function UsersTableUI({
  users,
  isLoading,
  isError,
  errorMessage,
  isDeactivating,
  deactivatingUserId,
  onAddUser,
  onDeactivate,
}: UsersTableUIProps) {
  const [confirmDeactivateUser, setConfirmDeactivateUser] = useState<UserRowViewModel | null>(
    null,
  );

  return (
    <>
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-heading-md text-primary-800">Usuarios registrados</h2>
          <p className="mt-1 max-w-2xl text-body-md text-gray-600">
            Listado institucional con nombre completo, contacto y rol. Las contraseñas no se
            almacenan en texto plano: solo podrá verlas al crear un usuario nuevo.
          </p>
        </div>
        <Button type="button" onClick={onAddUser} className="shrink-0">
          <UserPlus size={18} />
          Agregar usuario
        </Button>
      </div>

      {isError && (
        <div className="mb-4">
          <Alert variant="error">{errorMessage ?? 'No fue posible cargar el listado de usuarios.'}</Alert>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {['Nombre completo', 'Correo', 'Celular', 'Rol', 'Estado', 'Acciones'].map((header) => (
                <th
                  key={header}
                  className="px-4 py-3 text-left text-label-md font-medium uppercase tracking-wide text-gray-600"
                >
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-body">
            {isLoading && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-body-md text-gray-500">
                  Cargando usuarios…
                </td>
              </tr>
            )}

            {!isLoading && users.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-body-md text-gray-500">
                  No hay usuarios registrados. Use &quot;Agregar usuario&quot; para crear el primero.
                </td>
              </tr>
            )}

            {!isLoading &&
              users.map((user) => (
                <tr key={user.userId} className="hover:bg-gray-50/80">
                  <td className="px-4 py-3 text-body-md font-medium text-gray-900">{user.fullName}</td>
                  <td className="px-4 py-3 text-body-md text-gray-700">{user.email}</td>
                  <td className="px-4 py-3 text-body-md text-gray-700">{user.phoneNumber}</td>
                  <td className="px-4 py-3 text-body-md text-gray-700">{user.roleLabel}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-label-md font-medium ${
                        STATUS_STYLES[user.status] ?? 'bg-gray-100 text-gray-600'
                      }`}
                    >
                      {STATUS_LABELS[user.status] ?? user.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {user.canDeactivate ? (
                      <Button
                        variant="danger"
                        className="px-3 py-2"
                        isLoading={isDeactivating && deactivatingUserId === user.userId}
                        onClick={() => setConfirmDeactivateUser(user)}
                      >
                        Desactivar
                      </Button>
                    ) : (
                      <span className="text-label-md text-gray-500">—</span>
                    )}
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </section>

      <ConfirmDialog
        isOpen={confirmDeactivateUser !== null}
        title="Desactivar usuario"
        description={
          confirmDeactivateUser
            ? `¿Desactivar la cuenta de ${confirmDeactivateUser.fullName} (${confirmDeactivateUser.email})? El historial se conservará.`
            : ''
        }
        confirmLabel="Desactivar"
        isLoading={isDeactivating}
        onClose={() => setConfirmDeactivateUser(null)}
        onConfirm={() => {
          if (confirmDeactivateUser) {
            onDeactivate(confirmDeactivateUser.userId);
            setConfirmDeactivateUser(null);
          }
        }}
      />
    </>
  );
}
