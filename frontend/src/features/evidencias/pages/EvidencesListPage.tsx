import { Link } from 'react-router-dom';
import { Loader2, Upload } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { useSearchEvidences } from '../../../api/endpoints/evidence-query-controller/evidence-query-controller';
import { useAuth } from '../../../lib/auth/useAuth';

export function EvidencesListPage() {
  const { session } = useAuth();
  const { data, isLoading, isError } = useSearchEvidences({ page: 0, size: 20 });

  const items = data?.data?.content ?? [];

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="evidences" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div>
            <h1 className="text-heading-lg font-bold text-primary-800">Evidencias cargadas</h1>
            <p className="text-body-md text-gray-600">
              Datos demo del seed de desarrollo + evidencias que usted suba (API real).
            </p>
          </div>
          {session?.role === 'CC' && (
            <Link
              to="/evidencias/subir"
              className="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-label-md font-semibold text-body hover:bg-primary-700"
            >
              <Upload size={16} />
              Cargar nueva
            </Link>
          )}
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          {isLoading ? (
            <div className="flex items-center gap-2 text-gray-500">
              <Loader2 className="animate-spin" size={20} />
              Cargando evidencias...
            </div>
          ) : isError ? (
            <p className="text-body-md text-danger">
              No se pudo consultar evidencias. Verifique backend en :8080 e inicie sesión como CC o TD.
            </p>
          ) : items.length === 0 ? (
            <p className="text-body-md text-gray-500">
              No hay evidencias. Reinicie el backend para cargar el seed demo o suba una como CC.
            </p>
          ) : (
            <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-body shadow-sm">
              <table className="w-full text-left text-body-md">
                <thead className="bg-gray-50 text-label-sm uppercase text-gray-600">
                  <tr>
                    <th className="px-6 py-4">Indicador</th>
                    <th className="px-6 py-4">Descripción</th>
                    <th className="px-6 py-4">Versión</th>
                    <th className="px-6 py-4">Fase</th>
                    <th className="px-6 py-4">Creada</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {items.map((item) => (
                    <tr key={item.evidenceId} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <span className="font-semibold text-primary-800">{item.indicatorCode}</span>
                        <p className="text-label-md text-gray-500">{item.indicatorTitle}</p>
                      </td>
                      <td className="px-6 py-4 max-w-md truncate text-gray-700">{item.description}</td>
                      <td className="px-6 py-4">v{item.latestVersion}</td>
                      <td className="px-6 py-4">{item.phaseId}</td>
                      <td className="px-6 py-4 text-gray-600">{item.createdAt?.slice(0, 10)}</td>
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
