import { useParams } from 'react-router-dom';
import { Sidebar } from '../../../components/layout/Sidebar';
import { ProcessDetailView } from '../components/ProcessDetailView';

export function ProcessDetailPage() {
  const { processId } = useParams<{ processId: string }>();

  if (!processId) {
    return null;
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto px-8 py-8">
        <ProcessDetailView processId={processId} />
      </main>
    </div>
  );
}
