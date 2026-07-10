import { Link } from 'react-router-dom';
import { Loader2, Plus } from 'lucide-react';
import { Sidebar } from '../../components/layout/Sidebar';
import { useListProcesses } from '../../api/endpoints/accreditation-process-controller/accreditation-process-controller';

export function ProcessesListPage() {
  const { data, isLoading, isError } = useListProcesses();

  const processes = data?.data ?? [];

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div>
            <h1 className="text-heading-lg font-bold text-primary-800">Procesos de Acreditación</h1>
            <p className="text-body-md text-gray-600">Gestión de procesos activos, cerrados y archivados.</p>
          </div>
          <Link
            to="/procesos/nuevo"
            className="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-label-md font-semibold text-body hover:bg-primary-700"
          >
            <Plus size={16} />
            Nuevo proceso
          </Link>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          {isLoading ? (
            <div className="flex items-center gap-2 text-gray-500">
              <Loader2 className="animate-spin" size={20} />
              Cargando procesos...
            </div>
          ) : isError ? (
            <p className="text-body-md text-danger">No se pudo cargar el listado de procesos.</p>
          ) : processes.length === 0 ? (
            <p className="text-body-md text-gray-500">No hay procesos registrados.</p>
          ) : (
            <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-body shadow-sm">
              <table className="w-full text-left text-body-md">
                <thead className="bg-gray-50 text-label-sm uppercase text-gray-600">
                  <tr>
                    <th className="px-6 py-4">ID</th>
                    <th className="px-6 py-4">Periodo</th>
                    <th className="px-6 py-4">Tipo</th>
                    <th className="px-6 py-4">Estado</th>
                    <th className="px-6 py-4">Taxonomía</th>
                    <th className="px-6 py-4">Creado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {processes.map((process) => (
                    <tr key={process.processId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-mono text-xs text-primary-800">{process.processId}</td>
                      <td className="px-6 py-4">{process.period}</td>
                      <td className="px-6 py-4">{process.type}</td>
                      <td className="px-6 py-4">
                        <span className="rounded-full bg-primary-50 px-2.5 py-0.5 text-xs font-semibold text-primary-700">
                          {process.status}
                        </span>
                      </td>
                      <td className="px-6 py-4">{process.taxonomySnapshotVersion}</td>
                      <td className="px-6 py-4 text-gray-600">{process.createdAt?.slice(0, 10)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
