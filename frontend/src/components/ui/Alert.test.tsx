import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { Alert } from './Alert';

describe('Alert', () => {
  it('shouldExposeAccessibleAlertWithTitleAndMessage', () => {
    render(
      <Alert variant="error" title="Error de autenticación">
        Credenciales inválidas
      </Alert>,
    );

    const alert = screen.getByRole('alert');
    expect(alert).toHaveTextContent('Error de autenticación');
    expect(alert).toHaveTextContent('Credenciales inválidas');
  });
});
