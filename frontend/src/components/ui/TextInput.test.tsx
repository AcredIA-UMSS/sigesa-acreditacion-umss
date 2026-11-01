import { useState } from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { TextInput } from './TextInput';

function ControlledEmailInput() {
  const [value, setValue] = useState('');
  return (
    <TextInput
      label="Correo Institucional"
      value={value}
      onChange={(event) => setValue(event.target.value)}
    />
  );
}

describe('TextInput', () => {
  it('shouldAssociateLabelWithInputAndAcceptTyping', async () => {
    const user = userEvent.setup();
    render(<ControlledEmailInput />);

    const input = screen.getByLabelText('Correo Institucional');
    await user.type(input, 'cc@umss.edu.bo');
    expect(input).toHaveValue('cc@umss.edu.bo');
  });

  it('shouldShowValidationErrorText', () => {
    render(
      <TextInput
        label="Contraseña"
        error="La contraseña es obligatoria."
        value=""
        onChange={() => undefined}
      />,
    );

    expect(screen.getByText('La contraseña es obligatoria.')).toBeInTheDocument();
  });

  it('shouldMarkRequiredFields', () => {
    render(<TextInput label="Nombre" requiredMark value="" onChange={() => undefined} />);
    expect(screen.getByText('*')).toBeInTheDocument();
  });
});
