import { Sidebar } from '../../../components/layout/Sidebar';
import { ProcessListView } from '../components/ProcessListView';

export function ProcessListPage() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto px-8 py-8">
        <ProcessListView />
      </main>
    </div>
  );
}
