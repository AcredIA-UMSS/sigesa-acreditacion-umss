import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { IndicatorFodaForm } from './IndicatorFodaForm';

describe('IndicatorFodaForm', () => {
  it('permite a CC guardar FODA completo', async () => {
    const onSave = vi.fn();
    const user = userEvent.setup();
    render(<IndicatorFodaForm canEdit onSave={onSave} />);

    await user.selectOptions(screen.getByLabelText(/Cuadrante FODA/), 'WEAKNESS');
    await user.selectOptions(screen.getByLabelText(/Juicio de cumplimiento/), 'PARTIAL');
    await user.type(screen.getByLabelText(/Síntesis/), 'Hallazgo de debilidad');
    await user.click(screen.getByRole('button', { name: 'Guardar FODA' }));

    expect(onSave).toHaveBeenCalledWith({
      fodaQuadrant: 'WEAKNESS',
      compliance: 'PARTIAL',
      synthesis: 'Hallazgo de debilidad',
    });
  });

  it('muestra FODA en modo lectura para TD/JD', () => {
    render(
      <IndicatorFodaForm
        canEdit={false}
        onSave={vi.fn()}
        initialValues={{
          fodaQuadrant: 'STRENGTH',
          compliance: 'COMPLIES',
          synthesis: 'Cumple el criterio',
        }}
      />,
    );

    expect(screen.getByText('Fortaleza')).toBeInTheDocument();
    expect(screen.getByText('Cumple')).toBeInTheDocument();
    expect(screen.getByText('Cumple el criterio')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Guardar FODA' })).not.toBeInTheDocument();
  });
});
