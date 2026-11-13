import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { JdOrTdRoute } from './components/auth/JdOrTdRoute';
import { JdOnlyRoute } from './components/auth/JdOnlyRoute';
import { TdOnlyRoute } from './components/auth/TdOnlyRoute';
import { CcOnlyRoute } from './components/auth/CcOnlyRoute';
import { TemplatesListPage, TemplateEditorPage } from './features/admin/templates';
import { UsersAdminPage } from './features/admin/users/pages/UsersAdminPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { HomePage } from './features/home';
import { NewsAdminPage, PublicNewsDetailPage, PublicNewsListPage } from './features/news';
import { PublicStatusPage } from './features/public-portal';
import { CreateProcessPage } from './features/accreditation-process';
import { ProcessListPage, ProcessDetailPage, ProcessStructurePage } from './features/processes';
import { DashboardPage } from './features/dashboard/pages/DashboardPage';
import { EvidenceUploadPage } from './features/evidence/EvidenceUploadPage';
import { EvidenceSearchPage } from './features/evidence/EvidenceSearchPage';
import { ExecutiveReportPage } from './features/reports/ExecutiveReportPage';
import { ProcessPhasePage } from './features/reports/phase/ProcessPhasePage';
import { AssistantPage } from './features/assistant/AssistantPage';
import { TechnicianInboxPage } from './features/dashboard/pages/TechnicianInboxPage';
import { ExecutiveSemaphorePage } from './features/dashboard/pages/ExecutiveSemaphorePage';
import { AuditLogPage } from './features/admin/audit/pages/AuditLogPage';
import { AccessHistoryPage } from './features/admin/access/pages/AccessHistoryPage';

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
          path="/technician/inbox"
          element={
            <ProtectedRoute>
              <JdOrTdRoute>
                <TechnicianInboxPage />
              </JdOrTdRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/technician/inbox"
          element={
            <ProtectedRoute>
              <JdOrTdRoute>
                <TechnicianInboxPage />
              </JdOrTdRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/auditoria"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <AuditLogPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/ingresos"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <AccessHistoryPage />
              </JdOnlyRoute>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/audit"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <AuditLogPage />
              </JdOnlyRoute>
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
          path="/admin/noticias"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <NewsAdminPage />
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
          path="/evidencias/buscar"
          element={
            <ProtectedRoute>
              <EvidenceSearchPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/evidencias/search"
          element={
            <ProtectedRoute>
              <EvidenceSearchPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/executive/semaphore"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <ExecutiveSemaphorePage />
              </JdOnlyRoute>
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
          path="/reportes/fase"
          element={
            <ProtectedRoute>
              <TdOnlyRoute>
                <ProcessPhasePage />
              </TdOnlyRoute>
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

        <Route path="/" element={<HomePage />} />
        <Route path="/public/status" element={<PublicStatusPage />} />
        <Route path="/public/news" element={<PublicNewsListPage />} />
        <Route path="/public/news/:slug" element={<PublicNewsDetailPage />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
