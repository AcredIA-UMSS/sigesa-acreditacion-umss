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
      <Route path="/public/news" element={<p>Listado de noticias</p>} />
      <Route path="/public/status" element={<p>Estado de acreditación</p>} />
    </Routes>
  );
}

describe('HomePage', () => {
  it('renders the three CTAs with login, demo and help destinations', async () => {
    const user = userEvent.setup();
    renderWithProviders(<HomeRoutes />, { route: '/' });

    expect(screen.getByRole('heading', { level: 1, name: /Excelencia que/ })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Ir al inicio' })).toBeInTheDocument();

    const loginLinks = screen.getAllByRole('link', { name: /Iniciar Sesión/i });
    expect(loginLinks[0]).toHaveAttribute('href', '/login');

    const helpLinks = screen.getAllByRole('link', { name: 'Ayuda' });
    expect(helpLinks[0]).toHaveAttribute('href', '/login');

    await user.click(screen.getByRole('button', { name: 'Ver demo' }));
    expect(screen.getByText('Página de login')).toBeInTheDocument();
  });

  it('sends the header Estado de Acreditación link to the public status page', () => {
    renderWithProviders(<HomeRoutes />, { route: '/' });

    const statusLinks = screen.getAllByRole('link', { name: 'Estado de Acreditación' });
    expect(statusLinks[0]).toHaveAttribute('href', '/public/status');
  });

  it('sends the header Noticias link to the public news list', () => {
    renderWithProviders(<HomeRoutes />, { route: '/' });

    const newsLinks = screen.getAllByRole('link', { name: 'Noticias' });
    expect(newsLinks[0]).toHaveAttribute('href', '/public/news');
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
