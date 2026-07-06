import { Alert } from '../../../components/ui/Alert';

export function UsersTablePlaceholderUI() {
  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
      <div className="mb-6">
        <h2 className="text-heading-md text-primary-800">Usuarios registrados</h2>
        <p className="mt-1 text-body-md text-gray-600">
          Listado y acciones de revocación en fila pendientes de backend.
        </p>
      </div>

      <Alert variant="warning" title="Pendiente de GET /api/v1/admin/users">
        No existe contrato de listado en v1.0. Cuando el backend exponga{' '}
        <code className="text-code">GET /api/v1/admin/users</code> (idealmente con filtros opcionales
        por <code className="text-code">status</code> y <code className="text-code">role</code>),
        esta tabla mostrará usuarios existentes y habilitará la revocación (soft deactivate) sin
        simular ni persistir datos en cliente.
      </Alert>

      <div className="mt-6 overflow-hidden rounded-xl border border-gray-200">
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
          <tbody>
            <tr>
              <td colSpan={4} className="px-4 py-10 text-center text-body-md text-gray-500">
                Sin datos — endpoint de listado no disponible
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  );
}
