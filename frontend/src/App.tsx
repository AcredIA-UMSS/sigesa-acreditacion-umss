import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { JdOrTdRoute } from './components/auth/JdOrTdRoute';
import { JdOnlyRoute } from './components/auth/JdOnlyRoute';
import { CcOnlyRoute } from './components/auth/CcOnlyRoute';
import { TemplatesListPage, TemplateEditorPage } from './features/admin/templates';
import { UsersAdminPage } from './features/admin/users/pages/UsersAdminPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { CreateProcessPage } from './features/accreditation-process';
import { ProcessListPage, ProcessDetailPage, ProcessStructurePage } from './features/processes';
import { DashboardPage } from './features/dashboard/pages/DashboardPage';
import { EvidenceUploadPage } from './features/evidence/EvidenceUploadPage';
import { ExecutiveReportPage } from './features/reports/ExecutiveReportPage';
import { AssistantPage } from './features/assistant/AssistantPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/plantillas"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <TemplatesListPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/plantillas/nueva"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <TemplateEditorPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/plantillas/:templateId"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <TemplateEditorPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/users"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <UsersAdminPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/procesos/nuevo"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <CreateProcessPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/procesos"
          element={
            <ProtectedRoute>
              <ProcessListPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/procesos/:processId/estructura"
          element={
            <ProtectedRoute>
              <JdOrTdRoute>
                <ProcessStructurePage />
              </JdOrTdRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/procesos/:processId"
          element={
            <ProtectedRoute>
              <ProcessDetailPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/evidencias/cargar"
          element={
            <ProtectedRoute>
              <CcOnlyRoute>
                <EvidenceUploadPage />
              </CcOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/reportes/ejecutivo"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <ExecutiveReportPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/ayuda"
          element={
            <ProtectedRoute>
              <AssistantPage />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
