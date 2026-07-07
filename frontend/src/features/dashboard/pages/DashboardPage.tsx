import { useState, useEffect } from 'react';
import { useDashboardSummary } from '../api/dashboardHooks';
import { Sidebar } from '../../../components/layout/Sidebar';
import { CoordinatorDashboardSection } from '../components/CoordinatorDashboardSection';
import { TechnicianDashboardSection } from '../components/TechnicianDashboardSection';
import { ExecutiveDashboardSection } from '../components/ExecutiveDashboardSection';
import {
  ShieldCheck,
  XCircle,
  Users,
  AlertTriangle,
} from 'lucide-react';

export function DashboardPage() {
  const { summary, isLoading, error, refetch, mockPersona, changeMockPersona } = useDashboardSummary();
  const [activeTab, setActiveTab] = useState<'cc' | 'td' | 'jd' | null>(null);

  const hasCc = summary?.grantedPermissions?.includes('READ_CC_DASHBOARD') && summary?.coordinatorSection !== null && summary?.coordinatorSection !== undefined;
  const hasTd = summary?.grantedPermissions?.includes('READ_TD_DASHBOARD') && summary?.technicianSection !== null && summary?.technicianSection !== undefined;
  const hasJd = summary?.grantedPermissions?.includes('READ_JD_DASHBOARD') && summary?.executiveSection !== null && summary?.executiveSection !== undefined;

  // Sync activeTab when summary loads or changes (like after switching mock personas)
  useEffect(() => {
    if (summary) {
      if (hasCc) {
        setActiveTab('cc');
      } else if (hasTd) {
        setActiveTab('td');
      } else if (hasJd) {
        setActiveTab('jd');
      } else {
        setActiveTab(null);
      }
    }
  }, [summary, hasCc, hasTd, hasJd]);

  const handleReload = () => {
    refetch();
  };

  // Determine if user has no assigned sections
  const hasNoData = summary && !hasCc && !hasTd && !hasJd;

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

          <div className="flex flex-wrap items-center gap-3">
            {/* Mock Persona Switcher (Local Testing Option) */}
            {import.meta.env.DEV && (
              <div className="border-l pl-4 ml-4 border-amber-500">
                <div className="flex items-center gap-2 rounded-xl border border-primary-200/40 bg-body px-3 py-2 shadow-sm text-xs font-semibold text-primary-750">
                  <Users size={16} className="text-primary-500" />
                  <span className="text-[11px] text-gray-600 font-medium">Simular Rol:</span>
                  <select
                    value={mockPersona || 'real'}
                    onChange={(e) => {
                      const val = e.target.value;
                      changeMockPersona(val === 'real' ? null : val);
                    }}
                    className="bg-transparent focus:outline-none cursor-pointer text-label-md font-bold text-primary-800 border-l pl-2 border-gray-200/70 ml-1"
                  >
                    <option value="real">API Real (Sin Mock)</option>
                    <option value="CC">Coordinador de Carrera [CC]</option>
                    <option value="TD">Técnico DUEA [TD]</option>
                    <option value="JD">Jefatura DUEA [JD]</option>
                    <option value="MULTI">Multi-Rol [CC + TD]</option>
                  </select>
                </div>
              </div>
            )}

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
            {((hasCc ? 1 : 0) + (hasTd ? 1 : 0) + (hasJd ? 1 : 0)) > 1 && (
              <div className="flex border-b border-primary-200/50 pb-px mb-6">
                {hasCc && (
                  <button
                    type="button"
                    onClick={() => setActiveTab('cc')}
                    className={`border-b-2 px-6 py-3 text-body-lg font-semibold transition-all ${
                      activeTab === 'cc'
                        ? 'border-secondary text-primary-850 font-bold'
                        : 'border-transparent text-gray-600 hover:text-primary-800'
                    }`}
                  >
                    Coordinador de Carrera [CC]
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
            {activeTab === 'cc' && summary.coordinatorSection && (
              <CoordinatorDashboardSection section={summary.coordinatorSection} />
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
