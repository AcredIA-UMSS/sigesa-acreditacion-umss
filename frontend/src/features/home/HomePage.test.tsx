import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { seedSession } from '../../test/session';
import { renderWithProviders } from '../../test/test-utils';
import { HomePage } from './HomePage';

function HomeRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<p>Página de login</p>} />
      <Route path="/dashboard" element={<p>Dashboard</p>} />
      <Route path="/ayuda" element={<p>Ayuda</p>} />
      <Route path="/admin/users" element={<p>Admin JD</p>} />
    </Routes>
  );
}

describe('HomePage', () => {
  it('renders the three CTAs with login, demo and help destinations', async () => {
    const user = userEvent.setup();
    renderWithProviders(<HomeRoutes />, { route: '/' });

    expect(screen.getByRole('heading', { level: 1, name: /Excelencia que/ })).toBeInTheDocument();

    const loginLinks = screen.getAllByRole('link', { name: /Iniciar Sesión/i });
    expect(loginLinks[0]).toHaveAttribute('href', '/login');

    const helpLinks = screen.getAllByRole('link', { name: 'Ayuda' });
    expect(helpLinks[0]).toHaveAttribute('href', '/login');

    await user.click(screen.getByRole('button', { name: 'Ver demo' }));
    expect(screen.getByText('Página de login')).toBeInTheDocument();
  });

  it('sends help to the assistant when there is an active session', () => {
    seedSession('CC');
    renderWithProviders(<HomeRoutes />, { route: '/' });

    const helpLinks = screen.getAllByRole('link', { name: 'Ayuda' });
    expect(helpLinks[0]).toHaveAttribute('href', '/ayuda');
  });

  it('sends an authenticated session to the post-login panel on Ver demo', async () => {
    seedSession('CC');
    const user = userEvent.setup();
    renderWithProviders(<HomeRoutes />, { route: '/' });

    await user.click(screen.getByRole('button', { name: 'Ver demo' }));
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });
});
