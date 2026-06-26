import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import './App.css';
import { EvidenceUploadPanel } from './features/evidence/components/EvidenceUploadPanel';
import { ExecutiveReportPanel } from './features/reports/components/ExecutiveReportPanel';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <main className="min-h-screen space-y-10 bg-gray-100 py-10">
        <h1 className="text-center text-3xl font-bold text-gray-900">SIGESA</h1>
        <EvidenceUploadPanel />
        <ExecutiveReportPanel />
      </main>
    </QueryClientProvider>
  );
}

export default App;
