import { useParams, useSearchParams } from 'react-router-dom';
import { Sidebar } from '../../../components/layout/Sidebar';
import { ProcessDetailView } from '../components/ProcessDetailView';

export function ProcessDetailPage() {
  const { processId, indicatorId } = useParams<{ processId: string; indicatorId?: string }>();
  const [searchParams] = useSearchParams();
  const observationId = searchParams.get('observationId') ?? undefined;

  if (!processId) {
    return null;
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main data-app-scroll className="flex-1 overflow-y-auto px-8 py-8">
        <ProcessDetailView
          processId={processId}
          initialIndicatorId={indicatorId}
          initialObservationId={observationId}
        />
      </main>
    </div>
  );
}
