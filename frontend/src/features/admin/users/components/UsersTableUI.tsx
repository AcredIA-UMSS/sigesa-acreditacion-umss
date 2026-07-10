import { Filter } from 'lucide-react';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import type { UserRowViewModel } from '../hooks/useUsersList';

interface SelectOption {
  value: string;
  label: string;
}

interface UsersTableUIProps {
  users: UserRowViewModel[];
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  isDeactivating: boolean;
  deactivatingUserId: string | null;
  roleFilter: string;
  statusFilter: string;
  roleOptions: SelectOption[];
  statusOptions: readonly SelectOption[];
  onRoleFilterChange: (value: string) => void;
  onStatusFilterChange: (value: string) => void;
  onDeactivate: (userId: string) => void;
}

const STATUS_LABELS: Record<string, string> = {
  INACTIVE: 'Inactivo',
  ACTIVE: 'Activo',
  DEACTIVATED: 'Revocado',
};

export function UsersTableUI({
  users,
  isLoading,
  isError,
  errorMessage,
  isDeactivating,
  deactivatingUserId,
  roleFilter,
  statusFilter,
  roleOptions,
  statusOptions,
  onRoleFilterChange,
  onStatusFilterChange,
  onDeactivate,
}: UsersTableUIProps) {
  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-heading-md text-primary-800">Usuarios registrados</h2>
          <p className="mt-1 text-body-md text-gray-600">
            Listado institucional con revocación soft (estado DEACTIVATED).
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 rounded-xl border border-primary-200/40 bg-body px-3.5 py-2 shadow-sm">
            <Filter size={16} className="text-primary-500" />
            <select
              value={roleFilter}
              onChange={(e) => onRoleFilterChange(e.target.value)}
              className="cursor-pointer bg-transparent text-label-md font-semibold text-primary-800 focus:outline-none"
            >
              {roleOptions.map((option) => (
                <option key={option.value || 'all-roles'} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2 rounded-xl border border-primary-200/40 bg-body px-3.5 py-2 shadow-sm">
            <Filter size={16} className="text-primary-500" />
            <select
              value={statusFilter}
              onChange={(e) => onStatusFilterChange(e.target.value)}
              className="cursor-pointer bg-transparent text-label-md font-semibold text-primary-800 focus:outline-none"
            >
              {statusOptions.map((option) => (
                <option key={option.value || 'all-status'} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {isError && (
        <div className="mb-4">
          <Alert variant="error">{errorMessage ?? 'No fue posible cargar el listado de usuarios.'}</Alert>
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {['Correo', 'Rol', 'Estado', 'Acciones'].map((header) => (
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
                <td colSpan={4} className="px-4 py-10 text-center text-body-md text-gray-500">
                  Cargando usuarios…
                </td>
              </tr>
            )}

            {!isLoading && users.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-10 text-center text-body-md text-gray-500">
                  No hay usuarios registrados.
                </td>
              </tr>
            )}

            {!isLoading &&
              users.map((user) => (
                <tr key={user.userId}>
                  <td className="px-4 py-3 text-body-md text-gray-800">{user.email}</td>
                  <td className="px-4 py-3 text-body-md text-gray-700">{user.roleLabel}</td>
                  <td className="px-4 py-3 text-body-md text-gray-700">
                    {STATUS_LABELS[user.status] ?? user.status}
                  </td>
                  <td className="px-4 py-3">
                    {user.canDeactivate ? (
                      <Button
                        variant="danger"
                        className="px-3 py-2"
                        isLoading={isDeactivating && deactivatingUserId === user.userId}
                        onClick={() => {
                          const confirmed = window.confirm(
                            `¿Desactivar la cuenta ${user.email}? El historial se conservará.`,
                          );
                          if (confirmed) {
                            onDeactivate(user.userId);
                          }
                        }}
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
  );
}
