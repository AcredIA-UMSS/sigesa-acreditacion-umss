import { Sidebar } from '../../components/layout/Sidebar';
import { ProcessEvidenceSearchPanel } from './components/ProcessEvidenceSearchPanel';

export function EvidenceSearchPage() {
  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar activeNav="evidence" />
      <div className="flex min-w-0 flex-1 flex-col p-6">
        <h1 className="mb-4 text-heading-xl font-bold text-primary-900">Buscador de Evidencias</h1>
        <ProcessEvidenceSearchPanel />
      </div>
    </div>
  );
}
