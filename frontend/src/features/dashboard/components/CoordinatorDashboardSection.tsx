import { useState } from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  FolderDot,
  GraduationCap,
  TrendingUp,
  XCircle,
} from 'lucide-react';
import type { CoordinatorSection } from '../types';
import { ReportExportBar } from './ReportExportBar';
import { CoordinatorObservationsTable } from './CoordinatorObservationsTable';

interface CoordinatorDashboardSectionProps {
  section: CoordinatorSection;
}

export function CoordinatorDashboardSection({ section }: CoordinatorDashboardSectionProps) {
  const [phaseId, setPhaseId] = useState<number | undefined>(undefined);

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* CC Program Context Card */}
      <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
            <GraduationCap size={26} />
          </div>
          <div>
            <h2 className="text-heading-lg font-bold">
              {section.programName}
            </h2>
            <p className="text-body-md text-primary-100">
              Programa Scope ID: {section.programId}
            </p>
          </div>
        </div>
      </div>

      {/* KPI Grid */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-5">
        <KpiCard
          title="Indicadores"
          value={section.totalIndicadores}
          subtitle="Total asignados"
          icon={<FolderDot className="text-primary-500" size={24} />}
        />
        <KpiCard
          title="Avance Global"
          value={`${section.porcentajeAvanceGlobal}%`}
          subtitle="Progreso promedio"
          icon={<TrendingUp className="text-primary-500" size={24} />}
        />
        <KpiCard
          title="Evidencias Aprobadas"
          value={section.evidenciasAprobadas}
          subtitle="Revisadas y validadas"
          icon={<CheckCircle2 className="text-success" size={24} />}
        />
        <KpiCard
          title="Evidencias Rechazadas"
          value={section.evidenciasRechazadas}
          subtitle="Requieren acción"
          icon={<XCircle className="text-secondary" size={24} />}
        />
        <KpiCard
          title="Obs. Pendientes"
          value={section.observacionesPendientes}
          subtitle="Falta subsanación"
          icon={<Clock className="text-warning" size={24} />}
        />
      </div>

      {/* Phases and Bottlenecks Row */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Phase Progress Card */}
        <div className="lg:col-span-2 rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
          <h3 className="text-heading-sm font-bold text-primary-800 mb-6">
            Progreso de Fases del Proceso
          </h3>
          <div className="space-y-6">
            {section.fasesAvance.map((fase) => (
              <div key={fase.faseId} className="space-y-2">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-body-md font-semibold text-primary-800 block">
                      {fase.nombre}
                    </span>
                    <span
                      className={`inline-block text-[10px] uppercase font-bold mt-1 px-2 py-0.5 rounded-full ${
                        fase.estado === 'COMPLETADA'
                          ? 'bg-success/10 text-success'
                          : fase.estado === 'EN_PROCESO'
                          ? 'bg-warning/10 text-warning'
                          : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {fase.estado}
                    </span>
                  </div>
                  <span className="text-heading-sm font-bold text-primary-700">
                    {fase.porcentaje}%
                  </span>
                </div>
                <div className="h-2 w-full rounded-full bg-gray-100 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      fase.estado === 'COMPLETADA' ? 'bg-success' : 'bg-warning'
                    }`}
                    style={{ width: `${fase.porcentaje}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Bottleneck Alerts Card */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
          <h3 className="text-heading-sm font-bold text-primary-800 mb-4">
            Alertas de Cuello de Botella
          </h3>
          {section.cuellosDeBotella.length === 0 ? (
            <div className="flex h-48 flex-col items-center justify-center text-gray-500">
              <CheckCircle2 className="text-success mb-2" size={32} />
              <span className="text-body-md">No hay cuellos de botella</span>
            </div>
          ) : (
            <div className="space-y-4 max-h-[220px] overflow-y-auto pr-1">
              {section.cuellosDeBotella.map((cb) => (
                <div
                  key={cb.indicadorId}
                  className="flex gap-3 rounded-xl border border-secondary-500/10 bg-secondary-50/40 p-4"
                >
                  <AlertTriangle className="text-secondary shrink-0" size={20} />
                  <div>
                    <h4 className="text-body-md font-bold text-primary-800">
                      Indicador: {cb.indicadorId}
                    </h4>
                    <p className="text-xs text-gray-700 mt-0.5">
                      Criterio: {cb.codigoCriterio}
                    </p>
                    <span className="inline-block mt-2 rounded bg-secondary/10 px-2 py-0.5 text-xs font-semibold text-secondary-700">
                      Estancado por {cb.diasEstancado} días
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Report Export Control */}
      <ReportExportBar phaseId={phaseId} />

      {/* Observations Table */}
      <CoordinatorObservationsTable phaseId={phaseId} onPhaseIdChange={setPhaseId} />
    </div>
  );
}

// Local KPI Card Component
function KpiCard({
  title,
  value,
  subtitle,
  icon,
}: {
  title: string;
  value: string | number;
  subtitle: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-center justify-between">
        <span className="text-label-md font-semibold text-gray-700 uppercase">{title}</span>
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-50/50">
          {icon}
        </div>
      </div>
      <div className="mt-4">
        <h4 className="text-display-lg font-bold leading-none text-primary-800">{value}</h4>
        <p className="mt-2 text-xs text-gray-600">{subtitle}</p>
      </div>
    </div>
  );
}
