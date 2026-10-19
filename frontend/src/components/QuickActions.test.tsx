import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { renderWithProviders } from '../test/test-utils';
import { QuickActions } from './QuickActions';

describe('QuickActions', () => {
  it('shouldRenderThreeNavigationLinks', () => {
    renderWithProviders(<QuickActions />, { route: '/dashboard', withAuth: false });

    expect(screen.getByRole('heading', { name: 'Quick Actions' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Crear proceso/ })).toHaveAttribute('href', '/procesos/nuevo');
    expect(screen.getByRole('link', { name: /Subir evidencias/ })).toHaveAttribute(
      'href',
      '/evidencias/cargar',
    );
    expect(screen.getByRole('link', { name: /Generar reporte/ })).toHaveAttribute(
      'href',
      '/reportes/ejecutivo',
    );
  });
});
