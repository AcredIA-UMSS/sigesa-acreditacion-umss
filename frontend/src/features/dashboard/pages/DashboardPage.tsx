import { useState, useEffect } from 'react';
import { useDashboardSummary } from '../api/dashboardHooks';
import { Sidebar } from '../../../components/layout/Sidebar';
import { CoordinatorDashboardSection } from '../components/CoordinatorDashboardSection';
import { TechnicianDashboardSection } from '../components/TechnicianDashboardSection';
import { ExecutiveDashboardSection } from '../components/ExecutiveDashboardSection';
import {
  ShieldCheck,
  XCircle,
  AlertTriangle,
} from 'lucide-react';

import { useAuth } from '../../../lib/auth/useAuth';

export function DashboardPage() {
  const { session } = useAuth();
  const { summary, isLoading, error, refetch } = useDashboardSummary();
  const [activeTab, setActiveTab] = useState<'cc' | 'td' | 'jd' | null>(null);

  const permissions = (summary?.grantedPermissions as string[]) ?? [];
  const hasCc = (permissions.includes('READ_CC_DASHBOARD') || permissions.includes('ROLE_CC') || permissions.includes('CC')) && summary?.coordinatorSection != null;
  const hasEe = (permissions.includes('READ_EE_DASHBOARD') || permissions.includes('ROLE_EE') || permissions.includes('EE')) && summary?.coordinatorSection != null;
  const hasTd = (permissions.includes('READ_TD_DASHBOARD') || permissions.includes('ROLE_TD') || permissions.includes('TD')) && summary?.technicianSection != null;
  const hasJd = (permissions.includes('READ_JD_DASHBOARD') || permissions.includes('ROLE_JD') || permissions.includes('JD')) && summary?.executiveSection != null;
  const isExternalEvaluator = session?.role === 'EE';
  const showCoordinatorSection = (hasCc || hasEe) && summary?.coordinatorSection != null;

  // Sync activeTab when summary loads or changes (like after switching mock personas)
  useEffect(() => {
    if (summary) {
      if (hasCc || hasEe) {
        setActiveTab('cc');
      } else if (hasTd) {
        setActiveTab('td');
      } else if (hasJd) {
        setActiveTab('jd');
      } else {
        setActiveTab(null);
      }
    }
  }, [summary, hasCc, hasEe, hasTd, hasJd]);

  const handleReload = () => {
    refetch();
  };

  // Determine if user has no assigned sections
  const hasNoData = summary && !hasCc && !hasEe && !hasTd && !hasJd;

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-primary-900/10">
      <Sidebar activeNav="dashboard" />

      <main className="flex-1 overflow-y-auto px-8 py-8 text-primary-900 bg-gray-50/50">
        {/* Welcome Header */}
        <header className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div>
            <h1 className="text-heading-xl font-bold tracking-tight text-primary-800">
              {isExternalEvaluator ? 'Revisión Documental de Acreditación' : 'Panel de Control Acreditación'}
            </h1>
            <p className="mt-1 text-body-lg text-gray-700">
              {isExternalEvaluator
                ? 'SIGESA · Consulta de evidencias e indicadores de la carrera asignada (solo lectura)'
                : 'SIGESA · Gestión Integrada de Acreditación de Calidad Académica'}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">


            {summary && (
              <div className="flex items-center gap-2.5 rounded-xl border border-primary-200/40 bg-primary-100/35 px-4 py-2.5 text-primary-600 shadow-sm backdrop-blur-sm">
                <ShieldCheck size={18} className="text-secondary" />
                <span className="text-label-md font-semibold uppercase">
                  ID Usuario: {summary.userId}
                </span>
              </div>
            )}
          </div>
        </header>

        {/* Loading State Skeleton */}
        {isLoading && <DashboardSkeleton />}

        {/* Error States */}
        {error && !isLoading && (
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

        {/* No Permissions Notice */}
        {hasNoData && !isLoading && !error && (
          <div className="rounded-2xl border border-warning/20 bg-warning/5 p-8 text-center text-primary-950 shadow-sm max-w-xl mx-auto mt-12">
            <AlertTriangle className="mx-auto mb-4 text-warning" size={48} />
            <h3 className="text-heading-md font-bold">Acceso Restringido</h3>
            <p className="mt-2 text-body-md text-gray-700">
              No tiene permisos asignados para visualizar módulos del panel de control.
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Por favor, contacte al administrador del sistema para verificar su rol y privilegios.
            </p>
          </div>
        )}

        {/* Loaded Dashboard Content */}
        {!isLoading && !error && summary && !hasNoData && (
          <div className="space-y-8 animate-fadeIn">
            {/* Multi-Role Tabs Selector (if user has multiple roles) */}
            {((hasCc ? 1 : 0) + (hasEe ? 1 : 0) + (hasTd ? 1 : 0) + (hasJd ? 1 : 0)) > 1 && (
              <div className="flex border-b border-primary-200/50 pb-px mb-6">
                {(hasCc || hasEe) && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('cc')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'cc'
                        ? 'border-secondary text-primary-850 font-bold'
                        : 'border-transparent text-gray-600 hover:text-primary-800'
                    }`}
                  >
                    {hasEe && !hasCc ? 'Evaluador externo [EE]' : 'Coordinador de Carrera [CC]'}
                  </button>
                )}
                {hasTd && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('td')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'td'
                        ? 'border-secondary text-primary-850 font-bold'
                        : 'border-transparent text-gray-600 hover:text-primary-800'
                    }`}
                  >
                    Técnico DUEA [TD]
                  </button>
                )}
                {hasJd && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('jd')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'jd'
                        ? 'border-secondary text-primary-850 font-bold'
                        : 'border-transparent text-gray-600 hover:text-primary-800'
                    }`}
                  >
                    Jefatura DUEA [JD]
                  </button>
                )}
              </div>
            )}

            {/* Render Selected Role Dashboard */}
            {activeTab === 'cc' && showCoordinatorSection && summary.coordinatorSection && (
              <CoordinatorDashboardSection
                section={summary.coordinatorSection}
                readOnly={isExternalEvaluator || (hasEe && !hasCc)}
              />
            )}

            {activeTab === 'td' && summary.technicianSection && (
              <TechnicianDashboardSection section={summary.technicianSection} />
            )}

            {activeTab === 'jd' && summary.executiveSection && (
              <ExecutiveDashboardSection section={summary.executiveSection} />
            )}
          </div>
        )}
      </main>
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
