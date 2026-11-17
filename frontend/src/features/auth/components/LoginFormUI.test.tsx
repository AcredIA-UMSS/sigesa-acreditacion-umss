import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { LoginFormUI } from './LoginFormUI';

const noop = () => undefined;

describe('LoginFormUI', () => {
  it('shouldRenderWelcomeCopyAndSubmitCredentials', async () => {
    const user = userEvent.setup();
    const onEmailChange = vi.fn();
    const onSubmit = vi.fn();

    render(
      <LoginFormUI
        email=""
        password=""
        isSubmitting={false}
        onEmailChange={onEmailChange}
        onPasswordChange={noop}
        onSubmit={onSubmit}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Bienvenido' })).toBeInTheDocument();
    await user.type(screen.getByLabelText('Correo Institucional'), 'cc@umss.edu.bo');
    expect(onEmailChange).toHaveBeenCalled();
    await user.click(screen.getByRole('button', { name: /Iniciar sesión/ }));
    expect(onSubmit).toHaveBeenCalledOnce();
  });

  it('shouldShowFieldAndSubmitErrors', () => {
    render(
      <LoginFormUI
        email="user@gmail.com"
        password=""
        emailError="Solo se permiten correos institucionales @umss.edu.bo."
        passwordError="La contraseña es obligatoria."
        submitError="Credenciales inválidas"
        isSubmitting={false}
        onEmailChange={noop}
        onPasswordChange={noop}
        onSubmit={noop}
      />,
    );

    expect(screen.getByRole('alert')).toHaveTextContent('Credenciales inválidas');
    expect(screen.getByText('Solo se permiten correos institucionales @umss.edu.bo.')).toBeInTheDocument();
    expect(screen.getByText('La contraseña es obligatoria.')).toBeInTheDocument();
  });

  it('shouldDisableSubmitWhileSubmitting', () => {
    render(
      <LoginFormUI
        email="cc@umss.edu.bo"
        password="secret"
        isSubmitting
        onEmailChange={noop}
        onPasswordChange={noop}
        onSubmit={noop}
      />,
    );

    expect(screen.getByRole('button', { name: 'Procesando…' })).toBeDisabled();
  });
});
