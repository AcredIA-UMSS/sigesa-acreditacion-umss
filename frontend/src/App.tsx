import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { EvidenceUploadPage } from './features/evidence/EvidenceUploadPage';
import { CreateProcessPage } from './features/procesos/CreateProcessPage';
import { ExecutiveReportPanel } from './features/reports/components/ExecutiveReportPanel';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/procesos/nuevo" replace />} />
          <Route path="/procesos/nuevo" element={<CreateProcessPage />} />
          <Route path="/evidencias/cargar" element={<EvidenceUploadPage />} />
          <Route
            path="/dev/reportes"
            element={
              <main className="min-h-screen space-y-10 bg-gray-50 py-10">
                <ExecutiveReportPanel />
              </main>
            }
          />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
