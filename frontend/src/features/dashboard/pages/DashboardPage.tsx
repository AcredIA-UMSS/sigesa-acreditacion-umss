import { useState } from 'react';
import { useDashboardSummary } from '../api/dashboardHooks';
import { Sidebar } from '../../../components/layout/Sidebar';
import { CoordinatorObservationsTable } from '../components/CoordinatorObservationsTable';
import { ReportExportBar } from '../components/ReportExportBar';
import {
  Activity,
  AlertTriangle,
  Award,
  CheckCircle2,
  Clock,
  FileCheck,
  FolderDot,
  GraduationCap,
  ShieldCheck,
  TrendingUp,
  XCircle,
} from 'lucide-react';

export function DashboardPage() {
  const { summary, isLoading, error, refetch } = useDashboardSummary();
  const [activeTab, setActiveTab] = useState<'cc' | 'td' | 'jd' | null>(null);

  // Auto-select tab based on available sections
  const hasCc = summary?.coordinatorSection !== null && summary?.coordinatorSection !== undefined;
  const hasTd = summary?.technicianSection !== null && summary?.technicianSection !== undefined;
  const hasJd = summary?.executiveSection !== null && summary?.executiveSection !== undefined;

  // Set initial active tab if not set
  if (activeTab === null && summary) {
    if (hasCc) setActiveTab('cc');
    else if (hasTd) setActiveTab('td');
    else if (hasJd) setActiveTab('jd');
  }

  // Handle reload if error
  const handleReload = () => {
    refetch();
  };

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-primary-900/10">
      <Sidebar activeNav="dashboard" />

      <main className="flex-1 overflow-y-auto px-8 py-8 text-primary-900 bg-gray-50/50">
        {/* Welcome Header */}
        <header className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div>
            <h1 className="text-heading-xl font-bold tracking-tight text-primary-800">
              Panel de Control Acreditación
            </h1>
            <p className="mt-1 text-body-lg text-gray-700">
              SIGESA · Gestión Integrada de Acreditación de Calidad Académica
            </p>
          </div>

          {summary && (
            <div className="flex items-center gap-2.5 rounded-xl border border-primary-200/40 bg-primary-100/35 px-4 py-2 text-primary-600 shadow-sm backdrop-blur-sm">
              <ShieldCheck size={18} className="text-secondary" />
              <span className="text-label-md font-semibold uppercase">
                ID Usuario: {summary.userId}
              </span>
            </div>
          )}
        </header>

        {/* Loading State Skeleton */}
        {isLoading && <DashboardSkeleton />}

        {/* Error States */}
        {error && (
          <div className="rounded-2xl border border-secondary-500/20 bg-secondary-50 p-6 text-center text-secondary-900 shadow-sm max-w-xl mx-auto mt-12">
            <XCircle className="mx-auto mb-4 text-secondary-500" size={48} />
            <h3 className="text-heading-md font-bold">Error al Cargar Dashboard</h3>
            <p className="mt-2 text-body-md text-secondary-700">
              {(error as any).message || 'No posee los permisos PBAC requeridos para este dashboard.'}
            </p>
            <button
              type="button"
              onClick={handleReload}
              className="mt-6 rounded-xl bg-secondary hover:bg-secondary-600 px-5 py-2.5 text-label-md font-semibold text-body transition-colors"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Loaded Dashboard Content */}
        {!isLoading && !error && summary && (
          <div className="space-y-8 animate-fadeIn">
            {/* Multi-Role Tabs Selector (if user has multiple roles) */}
            {((hasCc ? 1 : 0) + (hasTd ? 1 : 0) + (hasJd ? 1 : 0)) > 1 && (
              <div className="flex border-b border-primary-200/50 pb-px mb-6">
                {hasCc && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('cc')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'cc'
                        ? 'border-secondary text-primary-800'
                        : 'border-transparent text-gray-700 hover:text-primary-800'
                    }`}
                  >
                    Coordinador de Carrera
                  </button>
                )}
                {hasTd && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('td')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'td'
                        ? 'border-secondary text-primary-800'
                        : 'border-transparent text-gray-700 hover:text-primary-800'
                    }`}
                  >
                    Comité Técnico
                  </button>
                )}
                {hasJd && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('jd')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'jd'
                        ? 'border-secondary text-primary-800'
                        : 'border-transparent text-gray-700 hover:text-primary-800'
                    }`}
                  >
                    Dirección Ejecutiva
                  </button>
                )}
              </div>
            )}

            {/* Render Selected Role Dashboard */}
            {activeTab === 'cc' && summary.coordinatorSection && (
              <div className="space-y-8">
                {/* CC Program Context Card */}
                <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
                  <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
                      <GraduationCap size={26} />
                    </div>
                    <div>
                      <h2 className="text-heading-lg font-bold">
                        {summary.coordinatorSection.programName}
                      </h2>
                      <p className="text-body-md text-primary-100">
                        Programa Scope ID: {summary.coordinatorSection.programId}
                      </p>
                    </div>
                  </div>
                </div>

                {/* KPI Grid */}
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-5">
                  <KpiCard
                    title="Indicadores"
                    value={summary.coordinatorSection.totalIndicadores}
                    subtitle="Total asignados"
                    icon={<FolderDot className="text-primary-500" size={24} />}
                  />
                  <KpiCard
                    title="Avance Global"
                    value={`${summary.coordinatorSection.porcentajeAvanceGlobal}%`}
                    subtitle="Progreso promedio"
                    icon={<TrendingUp className="text-primary-500" size={24} />}
                  />
                  <KpiCard
                    title="Evidencias Aprobadas"
                    value={summary.coordinatorSection.evidenciasAprobadas}
                    subtitle="Revisadas y validadas"
                    icon={<CheckCircle2 className="text-success" size={24} />}
                  />
                  <KpiCard
                    title="Evidencias Rechazadas"
                    value={summary.coordinatorSection.evidenciasRechazadas}
                    subtitle="Requieren acción"
                    icon={<XCircle className="text-secondary" size={24} />}
                  />
                  <KpiCard
                    title="Obs. Pendientes"
                    value={summary.coordinatorSection.observacionesPendientes}
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
                      {summary.coordinatorSection.fasesAvance.map((fase) => (
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
                    {summary.coordinatorSection.cuellosDeBotella.length === 0 ? (
                      <div className="flex h-48 flex-col items-center justify-center text-gray-500">
                        <CheckCircle2 className="text-success mb-2" size={32} />
                        <span className="text-body-md">No hay cuellos de botella</span>
                      </div>
                    ) : (
                      <div className="space-y-4 max-h-[220px] overflow-y-auto pr-1">
                        {summary.coordinatorSection.cuellosDeBotella.map((cb) => (
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
                <ReportExportBar phaseId={undefined} />

                {/* Observations Table */}
                <CoordinatorObservationsTable />
              </div>
            )}

            {/* TD Technician Dashboard */}
            {activeTab === 'td' && summary.technicianSection && (
              <div className="space-y-6">
                <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
                  <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
                      <Activity size={26} />
                    </div>
                    <div>
                      <h2 className="text-heading-lg font-bold">Bandeja Técnica del Comité</h2>
                      <p className="text-body-md text-primary-100">
                        Revisión y validación de evidencias académicas cargadas por coordinadores.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="grid gap-6 md:grid-cols-2">
                  <KpiCard
                    title="Evidencias Pendientes"
                    value={summary.technicianSection.evidenciasPendientesRevision}
                    subtitle="Por revisar y calificar"
                    icon={<FileCheck className="text-warning" size={28} />}
                  />
                  <KpiCard
                    title="Indicadores Asignados"
                    value={summary.technicianSection.indicadoresAsignados}
                    subtitle="Área técnica asignada"
                    icon={<Award className="text-primary-500" size={28} />}
                  />
                </div>

                <div className="rounded-2xl border border-primary-200/40 bg-body p-6 text-center text-gray-500 shadow-sm py-16">
                  <FolderDot className="mx-auto mb-4 text-primary-300" size={48} />
                  <h4 className="text-heading-sm font-bold text-primary-800">
                    Bandeja de Entrada Técnica
                  </h4>
                  <p className="mt-2 text-body-md max-w-md mx-auto text-gray-700">
                    Las tareas de revisión técnica están cargadas. Acceda a la opción "Gestión
                    Procesos" en la barra lateral para comenzar la validación de expedientes.
                  </p>
                </div>
              </div>
            )}

            {/* JD Executive Dashboard */}
            {activeTab === 'jd' && summary.executiveSection && (
              <div className="space-y-6">
                <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
                  <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
                      <Award size={26} />
                    </div>
                    <div>
                      <h2 className="text-heading-lg font-bold">Panel de Acreditación Ejecutiva</h2>
                      <p className="text-body-md text-primary-100">
                        Consolidación institucional de calidad de programas.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm py-16 text-center text-gray-500">
                  <TrendingUp className="mx-auto mb-4 text-primary-300" size={48} />
                  <h4 className="text-heading-sm font-bold text-primary-800">
                    Reporte General de Decanato
                  </h4>
                  <p className="mt-2 text-body-md max-w-md mx-auto text-gray-700">
                    Acceso concedido a KPIs agregados. La expansión de widgets ejecutivos para el
                    semáforo de programas está planificada para el Sprint v1.1.
                  </p>
                </div>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

// KPI Card Component
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

// skeleton loader
function DashboardSkeleton() {
  return (
    <div className="space-y-8 animate-pulse">
      <div className="h-24 w-full rounded-2xl bg-gray-200" />
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-5">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-32 rounded-2xl bg-gray-200" />
        ))}
      </div>
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="h-64 lg:col-span-2 rounded-2xl bg-gray-200" />
        <div className="h-64 rounded-2xl bg-gray-200" />
      </div>
      <div className="h-20 w-full rounded-2xl bg-gray-200" />
      <div className="h-64 w-full rounded-2xl bg-gray-200" />
    </div>
  );
}
