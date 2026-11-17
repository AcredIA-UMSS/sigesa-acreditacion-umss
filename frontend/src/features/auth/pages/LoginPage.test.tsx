import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { LoginPage } from './LoginPage';
import { seedSession } from '../../../test/session';
import { renderWithProviders } from '../../../test/test-utils';

describe('LoginPage', () => {
  it('shouldShowLoginFormWhenUserIsAnonymous', () => {
    renderWithProviders(<LoginPage />, { route: '/login' });
    expect(screen.getByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
  });

  it('shouldRedirectAuthenticatedCcToDashboard', () => {
    seedSession('CC');
    renderWithProviders(<LoginPage />, { route: '/login' });
    expect(screen.queryByRole('heading', { name: 'Bienvenido' })).not.toBeInTheDocument();
  });

  it('shouldValidateInstitutionalEmailBeforeCallingApi', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />, { route: '/login' });

    await user.type(screen.getByLabelText('Correo Institucional'), 'user@gmail.com');
    await user.type(screen.getByLabelText('Contraseña'), 'Secret123');
    await user.click(screen.getByRole('button', { name: /Iniciar sesión/ }));

    expect(screen.getByText('Solo se permiten correos institucionales @umss.edu.bo.')).toBeInTheDocument();
  });

  it('shouldShowInvalidCredentialsFromApi', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />, { route: '/login' });

    await user.type(screen.getByLabelText('Correo Institucional'), 'cc@umss.edu.bo');
    await user.type(screen.getByLabelText('Contraseña'), 'wrong');
    await user.click(screen.getByRole('button', { name: /Iniciar sesión/ }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales inválidas');
  });

  it('shouldAuthenticateActiveUserAndLeaveLoginForm', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />, { route: '/login' });

    await user.type(screen.getByLabelText('Correo Institucional'), 'cc@umss.edu.bo');
    await user.type(screen.getByLabelText('Contraseña'), 'CoordDemo2026!');
    await user.click(screen.getByRole('button', { name: /Iniciar sesión/ }));

    await waitFor(() => {
      expect(screen.queryByRole('heading', { name: 'Bienvenido' })).not.toBeInTheDocument();
    });
  });
});
