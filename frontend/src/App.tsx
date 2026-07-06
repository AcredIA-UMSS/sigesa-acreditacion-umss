import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { JdOnlyRoute } from './components/auth/JdOnlyRoute';
import { UsersAdminPage } from './features/admin/users/pages/UsersAdminPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { CreateProcessPage } from './features/procesos/CreateProcessPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

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

        <Route path="/" element={<Navigate to="/procesos/nuevo" replace />} />
        <Route path="*" element={<Navigate to="/procesos/nuevo" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
