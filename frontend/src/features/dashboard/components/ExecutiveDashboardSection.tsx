import {
  Award,
  TrendingUp,
  Building2,
  AlertOctagon,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';
import type { ExecutiveSection } from '../types';

interface ExecutiveDashboardSectionProps {
  section: ExecutiveSection;
}

export function ExecutiveDashboardSection({ section }: ExecutiveDashboardSectionProps) {
  const programs = section.semaforoProgramas ?? [];

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header Banner */}
      <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
            <Award size={26} />
          </div>
          <div>
            <h2 className="text-heading-lg font-bold">Panel de Acreditación Ejecutiva</h2>
            <p className="text-body-md text-primary-100">
              Jefatura DUEA [JD] · Consolidación y supervisión institucional de calidad académica.
            </p>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {/* Total Programs */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Programas en Acreditación</span>
            <h4 className="text-display-lg font-bold leading-none text-primary-800 mt-2">
              {section.totalProgramasEnAcreditacion ?? 0}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Carreras activas en el proceso de evaluación de calidad</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary-50 text-primary-500">
            <Building2 size={32} />
          </div>
        </div>

        {/* Institutional Progress */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Avance Institucional</span>
            <h4 className="text-display-lg font-bold leading-none text-primary-800 mt-2">
              {(section.porcentajeAvanceInstitucional ?? 0).toFixed(2)}%
            </h4>
            <p className="mt-2 text-xs text-gray-600">Progreso agregado a nivel institucional</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-success/10 text-success">
            <TrendingUp size={32} />
          </div>
        </div>

        {/* Critical Observations */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Observaciones Críticas</span>
            <h4 className="text-display-lg font-bold leading-none text-secondary-650 mt-2">
              {section.criticalObservations ?? 0}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Observaciones pendientes en toda la institución</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-secondary-100/50 text-secondary-500">
            <AlertTriangle size={32} />
          </div>
        </div>

        {/* Alert Programs */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between">
          <div>
            <span className="text-label-md font-semibold text-gray-700 uppercase">Programas en Alerta</span>
            <h4 className="text-display-lg font-bold leading-none text-warning mt-2">
              {section.alertPrograms ?? 0}
            </h4>
            <p className="mt-2 text-xs text-gray-600">Programas con semáforo Rojo o Amarillo</p>
          </div>
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-warning/10 text-warning">
            <AlertOctagon size={32} />
          </div>
        </div>
      </div>

      {/* Program Traffic Light (Semáforo de Programas) */}
      <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
        <div className="mb-6">
          <h3 className="text-heading-sm font-bold text-primary-800">Semáforo de Calidad de Programas</h3>
          <p className="text-body-md text-gray-600 mt-1">
            Visualización del estado de acreditación de carreras y alertas críticas para toma de decisiones ejecutivas.
          </p>
        </div>

        {programs.length === 0 ? (
          <div className="flex h-48 flex-col items-center justify-center text-gray-500">
            <CheckCircle2 className="text-success mb-2" size={36} />
            <span className="text-body-md">Todos los programas se encuentran en orden</span>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-body-md">
              <thead>
                <tr className="border-b border-gray-100 text-label-sm uppercase font-semibold text-gray-600">
                  <th className="pb-3">Carrera / Programa</th>
                  <th className="pb-3">Estado Acreditación</th>
                  <th className="pb-3">Obs. Críticas</th>
                  <th className="pb-3 text-right">Semáforo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {programs.map((prog) => {
                  let badgeColor = '';
                  let trafficLightColor = '';
                  let label = '';

                  switch (prog.estado) {
                    case 'VERDE':
                      badgeColor = 'bg-success/10 text-success border border-success/20';
                      trafficLightColor = 'bg-success shadow-success-glow';
                      label = 'Saludable';
                      break;
                    case 'AMARILLO':
                      badgeColor = 'bg-warning/10 text-warning border border-warning/20';
                      trafficLightColor = 'bg-warning shadow-warning-glow';
                      label = 'En Alerta';
                      break;
                    case 'ROJO':
                      badgeColor = 'bg-secondary/10 text-secondary border border-secondary/20';
                      trafficLightColor = 'bg-secondary shadow-secondary-glow';
                      label = 'Crítico';
                      break;
                    default:
                      badgeColor = 'bg-gray-100 text-gray-500 border border-gray-200';
                      trafficLightColor = 'bg-gray-400';
                      label = 'Indefinido';
                  }

                  return (
                    <tr key={prog.programaId} className="hover:bg-gray-50/50 transition-colors">
                      <td className="py-4">
                        <div className="font-semibold text-primary-800">{prog.nombre}</div>
                        <div className="text-xs text-gray-500">ID: {prog.programaId}</div>
                      </td>
                      <td className="py-4">
                        <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ${badgeColor}`}>
                          {prog.estado === 'VERDE' && <CheckCircle2 size={12} />}
                          {prog.estado === 'AMARILLO' && <AlertTriangle size={12} />}
                          {prog.estado === 'ROJO' && <AlertOctagon size={12} />}
                          {label}
                        </span>
                      </td>
                      <td className="py-4">
                        <span className={`text-label-md font-bold ${prog.observacionesCriticas > 0 ? 'text-secondary font-extrabold' : 'text-gray-500'}`}>
                          {prog.observacionesCriticas} observaciones
                        </span>
                      </td>
                      <td className="py-4 text-right">
                        <div className="inline-flex items-center justify-end">
                          <div className={`h-4 w-4 rounded-full ${trafficLightColor}`} title={`Estado: ${prog.estado}`} />
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
