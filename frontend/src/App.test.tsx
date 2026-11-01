import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import App from './App';
import { seedSession } from './test/session';
import { createTestQueryClient } from './test/test-utils';
import { QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './lib/auth/AuthProvider';
import { render } from '@testing-library/react';

function renderApp(path: string) {
  window.history.pushState({}, '', path);
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('App routing', () => {
  it('shouldShowPublicLoginRoute', async () => {
    renderApp('/login');
    expect(await screen.findByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
  });

  it('shouldRedirectAnonymousUsersFromProtectedDashboard', async () => {
    renderApp('/dashboard');
    expect(await screen.findByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
  });

  it('shouldRedirectUnknownPathsTowardDashboardThenLoginWhenAnonymous', async () => {
    renderApp('/ruta-inexistente');
    expect(await screen.findByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
  });

  it('shouldRenderDashboardForAuthenticatedCoordinator', async () => {
    seedSession('CC');
    renderApp('/dashboard');
    expect(await screen.findByText('Ingeniería de Sistemas')).toBeInTheDocument();
  });
});
