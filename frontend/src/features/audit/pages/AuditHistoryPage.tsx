import { Navigate, useLocation } from 'react-router-dom';
import { Bell, Settings } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { AuditHistoryPanel } from '../components/AuditHistoryPanel';
import { findAuditHistoryView } from '../historyRoutes';

export function AuditHistoryPage() {
  const location = useLocation();
  const view = findAuditHistoryView(location.pathname);

  if (!view) {
    return <Navigate to="/admin/historico" replace />;
  }

  return (
    <div className="flex h-screen bg-gray-50" data-testid="audit-history-page">
      <Sidebar activeNav="history" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div className="text-body-md text-gray-500">
            <span className="text-primary-600">Inicio</span>
            {' / Histórico / '}
            <span data-testid="audit-history-breadcrumb">{view.title}</span>
          </div>
          <div className="flex items-center gap-4 text-gray-600">
            <button type="button" className="relative hover:text-primary-600" aria-label="Notificaciones">
              <Bell size={24} />
              <span className="absolute right-0 top-0 h-2 w-2 rounded-full bg-secondary" />
            </button>
            <button type="button" className="hover:text-primary-600" aria-label="Configuración">
              <Settings size={24} />
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-7xl">
            <div className="mb-4 h-1 w-12 bg-secondary" />
            <h1 className="text-heading-xl text-primary-800" data-testid="audit-history-title">
              {view.title}
            </h1>
            <p className="mt-2 text-body-lg text-gray-600">{view.description}</p>
            <div className="mt-8">
              <AuditHistoryPanel view={view} />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
