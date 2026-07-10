import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { JdOnlyRoute } from './components/auth/JdOnlyRoute';
import { RoleRoute } from './components/auth/RoleRoute';
import { UsersAdminPage } from './features/admin/users/pages/UsersAdminPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { CreateProcessPage } from './features/procesos/CreateProcessPage';
import { ProcessesListPage } from './features/procesos/ProcessesListPage';
import { DashboardPage } from './features/dashboard/pages/DashboardPage';
import { EvidenceUploadPage } from './features/evidencias/pages/EvidenceUploadPage';
import { EvidencesListPage } from './features/evidencias/pages/EvidencesListPage';
import { IndicatorsCatalogPage, WorkflowReviewPage } from './features/workflow/pages/WorkflowPages';

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
          path="/procesos"
          element={
            <ProtectedRoute>
              <JdOnlyRoute>
                <ProcessesListPage />
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
          path="/procesos/evaluacion"
          element={
            <ProtectedRoute>
              <RoleRoute allowed={['TD']}>
                <WorkflowReviewPage />
              </RoleRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/indicadores"
          element={
            <ProtectedRoute>
              <RoleRoute allowed={['CC', 'TD', 'JD']}>
                <IndicatorsCatalogPage />
              </RoleRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/evidencias"
          element={
            <ProtectedRoute>
              <RoleRoute allowed={['CC', 'TD']}>
                <EvidencesListPage />
              </RoleRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/evidencias/subir"
          element={
            <ProtectedRoute>
              <RoleRoute allowed={['CC']}>
                <EvidenceUploadPage />
              </RoleRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="/evidencias/:indicatorId/subsanar"
          element={
            <ProtectedRoute>
              <RoleRoute allowed={['CC']}>
                <EvidenceUploadPage />
              </RoleRoute>
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
