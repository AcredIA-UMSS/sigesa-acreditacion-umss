import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { JdOnlyRoute } from './components/auth/JdOnlyRoute';
import { UsersAdminPage } from './features/admin/users/pages/UsersAdminPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { CreateProcessPage } from './features/procesos/CreateProcessPage';
import { DashboardPage } from './features/dashboard/pages/DashboardPage';

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
          path="/procesos/nuevo"
          element={
            <ProtectedRoute>
              <CreateProcessPage />
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
