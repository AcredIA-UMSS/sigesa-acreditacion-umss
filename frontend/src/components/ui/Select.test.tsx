import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { Select } from './Select';

describe('Select', () => {
  it('shouldRenderOptionsAndAllowChangingValue', async () => {
    const user = userEvent.setup();
    render(
      <Select
        label="Rol"
        defaultValue="CC"
        options={[
          { value: 'CC', label: 'Coordinador' },
          { value: 'TD', label: 'Técnico' },
        ]}
      />,
    );

    const select = screen.getByLabelText('Rol');
    await user.selectOptions(select, 'TD');
    expect(select).toHaveValue('TD');
  });

  it('shouldShowErrorInsteadOfHelperText', () => {
    render(
      <Select
        label="Carrera"
        options={[{ value: '', label: 'Seleccione' }]}
        helperText="Opcional"
        error="Seleccione la carrera asignada."
      />,
    );

    expect(screen.getByText('Seleccione la carrera asignada.')).toBeInTheDocument();
    expect(screen.queryByText('Opcional')).not.toBeInTheDocument();
  });
});
