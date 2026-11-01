import { screen } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';
import { JdOnlyRoute } from './JdOnlyRoute';
import { CcOnlyRoute } from './CcOnlyRoute';
import { JdOrTdRoute } from './JdOrTdRoute';
import { seedSession } from '../../test/session';
import { renderWithProviders } from '../../test/test-utils';

function SecretPage() {
  return <h1>Área protegida</h1>;
}

describe('route guards', () => {
  it('shouldRedirectAnonymousUsersToLogin', () => {
    renderWithProviders(
      <Routes>
        <Route path="/login" element={<p>Página de login</p>} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <SecretPage />
            </ProtectedRoute>
          }
        />
      </Routes>,
      { route: '/dashboard' },
    );

    expect(screen.getByText('Página de login')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Área protegida' })).not.toBeInTheDocument();
  });

  it('shouldAllowAuthenticatedUsers', () => {
    seedSession('CC');
    renderWithProviders(
      <Routes>
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <SecretPage />
            </ProtectedRoute>
          }
        />
      </Routes>,
      { route: '/dashboard' },
    );

    expect(screen.getByRole('heading', { name: 'Área protegida' })).toBeInTheDocument();
  });

  it('shouldKeepJdOnAdminRouteAndRedirectCc', () => {
    seedSession('CC');
    renderWithProviders(
      <Routes>
        <Route path="/dashboard" element={<p>Dashboard CC</p>} />
        <Route
          path="/admin/users"
          element={
            <JdOnlyRoute>
              <p>Admin JD</p>
            </JdOnlyRoute>
          }
        />
      </Routes>,
      { route: '/admin/users' },
    );

    expect(screen.getByText('Dashboard CC')).toBeInTheDocument();
  });

  it('shouldAllowTdOnJdOrTdRouteAndRejectCc', () => {
    seedSession('TD');
    const { unmount } = renderWithProviders(
      <Routes>
        <Route path="/dashboard" element={<p>Dashboard</p>} />
        <Route
          path="/procesos/1/estructura"
          element={
            <JdOrTdRoute>
              <p>Editor estructura</p>
            </JdOrTdRoute>
          }
        />
      </Routes>,
      { route: '/procesos/1/estructura' },
    );
    expect(screen.getByText('Editor estructura')).toBeInTheDocument();
    unmount();

    seedSession('CC');
    renderWithProviders(
      <Routes>
        <Route path="/dashboard" element={<p>Dashboard CC</p>} />
        <Route
          path="/procesos/1/estructura"
          element={
            <JdOrTdRoute>
              <p>Editor estructura</p>
            </JdOrTdRoute>
          }
        />
      </Routes>,
      { route: '/procesos/1/estructura' },
    );
    expect(screen.getByText('Dashboard CC')).toBeInTheDocument();
  });

  it('shouldAllowOnlyCcOnEvidenceUploadRoute', () => {
    seedSession('JD');
    renderWithProviders(
      <Routes>
        <Route path="/admin/users" element={<p>Admin JD</p>} />
        <Route
          path="/evidencias/cargar"
          element={
            <CcOnlyRoute>
              <p>Carga CC</p>
            </CcOnlyRoute>
          }
        />
      </Routes>,
      { route: '/evidencias/cargar' },
    );

    expect(screen.getByText('Admin JD')).toBeInTheDocument();
  });
});
