import {
  Activity,
  Award,
  CheckCircle2,
  ExternalLink,
  FileCheck,
  FolderDot,
  XCircle,
  AlertTriangle,
} from 'lucide-react';
import type { TechnicianSection } from '../types';

interface TechnicianDashboardSectionProps {
  section: TechnicianSection;
}

export function TechnicianDashboardSection({ section }: TechnicianDashboardSectionProps) {
  const evaluations = section.ultimasEvaluaciones ?? [];

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header Banner */}
      <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
            <Activity size={26} />
          </div>
          <div>
            <h2 className="text-heading-lg font-bold">Bandeja Técnica del Comité</h2>
            <p className="text-body-md text-primary-100">
              Gestión de calidad: Técnico DUEA [TD] · Revisión y validación de evidencias académicas.
            </p>
          </div>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {/* Card 1: Evidencias Pendientes */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Evidencias Pendientes</span>
            <h4 className="text-display-lg font-bold leading-none text-primary-800 mt-2">
              {section.evidenciasPendientesRevision}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Por revisar y calificar en el ciclo activo</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-warning/10 text-warning">
            <FileCheck size={32} />
          </div>
        </div>

        {/* Card 2: Indicadores Asignados */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Indicadores Asignados</span>
            <h4 className="text-display-lg font-bold leading-none text-primary-800 mt-2">
              {section.indicadoresAsignados}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Áreas de evaluación técnica bajo supervisión</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary-50 text-primary-500">
            <Award size={32} />
          </div>
        </div>

        {/* Card 3: Acciones Abiertas */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Acciones Abiertas</span>
            <h4 className="text-display-lg font-bold leading-none text-secondary-650 mt-2">
              {section.openActions ?? 0}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Observaciones que requieren atención</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-secondary-100/50 text-secondary-500">
            <AlertTriangle size={32} />
          </div>
        </div>

        {/* Card 4: Disponibles */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Disponibles</span>
            <h4 className="text-display-lg font-bold leading-none text-indigo-700 mt-2">
              {section.available ?? 0}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Evidencias listas para validación</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600">
            <CheckCircle2 size={32} />
          </div>
        </div>
      </div>

      {/* Quick Actions & Recent Evaluations Row */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Quick Actions */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-heading-sm font-bold text-primary-800 mb-2">Acciones Rápidas</h3>
            <p className="text-body-md text-gray-600 mb-6">
              Acceda directamente a las colas de validación y flujos de revisión de documentación.
            </p>
          </div>
          <div className="space-y-3">
            <a
              href="/procesos/evaluacion"
              className="flex items-center justify-between rounded-xl bg-primary-600 hover:bg-primary-700 text-body p-4 text-label-md font-semibold transition-all hover:translate-x-1"
            >
              <span>Ir a Bandeja de Revisión</span>
              <ExternalLink size={16} />
            </a>
            <a
              href="/indicadores"
              className="flex items-center justify-between rounded-xl border border-primary-200 hover:bg-gray-50 text-primary-700 p-4 text-label-md font-semibold transition-all hover:translate-x-1"
            >
              <span>Ver Criterios e Indicadores</span>
              <FolderDot size={16} className="text-primary-500" />
            </a>
          </div>
        </div>

        {/* Recent Evaluations Table */}
        <div className="lg:col-span-2 rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
          <h3 className="text-heading-sm font-bold text-primary-800 mb-4">
            Últimas Evaluaciones Realizadas
          </h3>
          {evaluations.length === 0 ? (
            <div className="flex h-48 flex-col items-center justify-center text-gray-500">
              <FolderDot className="text-gray-300 mb-2" size={36} />
              <span className="text-body-md">No hay evaluaciones recientes registradas</span>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-body-md">
                <thead>
                  <tr className="border-b border-gray-100 text-label-sm uppercase font-semibold text-gray-600">
                    <th className="pb-3">Evidencia ID</th>
                    <th className="pb-3">Programa</th>
                    <th className="pb-3">Fecha Revisión</th>
                    <th className="pb-3 text-right">Resultado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {evaluations.map((evalItem) => (
                    <tr key={evalItem.evidenciaId} className="hover:bg-gray-50/50 transition-colors">
                      <td className="py-3 font-semibold text-primary-800">{evalItem.evidenciaId}</td>
                      <td className="py-3 text-gray-700">{evalItem.programa}</td>
                      <td className="py-3 text-gray-600">{evalItem.fechaRevision}</td>
                      <td className="py-3 text-right">
                        <span
                          className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            evalItem.resultado === 'APROBADO'
                              ? 'bg-success/10 text-success'
                              : 'bg-secondary/10 text-secondary'
                          }`}
                        >
                          {evalItem.resultado === 'APROBADO' ? (
                            <CheckCircle2 size={12} />
                          ) : (
                            <XCircle size={12} />
                          )}
                          {evalItem.resultado}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
