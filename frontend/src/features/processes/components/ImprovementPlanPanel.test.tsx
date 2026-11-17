import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ImprovementPlanPanel } from './ImprovementPlanPanel';

describe('ImprovementPlanPanel', () => {
  it('muestra tabla e importa desde FODA cuando el rol puede editar', async () => {
    const onImport = vi.fn();
    const user = userEvent.setup();
    render(
      <ImprovementPlanPanel
        items={[
          {
            id: 'item-1',
            objective: 'Reducir deserción',
            activity: 'Tutorías',
            progress: 20,
            sourceIndicatorId: 'ind-1',
          },
        ]}
        canEdit
        isLoading={false}
        isError={false}
        isSaving={false}
        isImporting={false}
        onRefresh={vi.fn()}
        onImport={onImport}
        onCreate={vi.fn()}
        onUpdate={vi.fn()}
      />,
    );

    expect(screen.getByText('Plan de Mejoras')).toBeInTheDocument();
    expect(screen.getByText('Reducir deserción')).toBeInTheDocument();
    expect(screen.getByText('FODA')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Importar desde FODA/i }));
    expect(onImport).toHaveBeenCalled();
  });

  it('permite alta manual de ítem', async () => {
    const onCreate = vi.fn();
    const user = userEvent.setup();
    render(
      <ImprovementPlanPanel
        items={[]}
        canEdit
        isLoading={false}
        isError={false}
        isSaving={false}
        isImporting={false}
        onRefresh={vi.fn()}
        onImport={vi.fn()}
        onCreate={onCreate}
        onUpdate={vi.fn()}
      />,
    );

    await user.type(screen.getByLabelText(/Nuevo objetivo/i), 'Mejorar laboratorios');
    await user.click(screen.getByRole('button', { name: /Añadir ítem/i }));
    expect(onCreate).toHaveBeenCalledWith(
      expect.objectContaining({ objective: 'Mejorar laboratorios' }),
    );
  });

  it('muestra justificación y permite avance en modo monitoreo', () => {
    render(
      <ImprovementPlanPanel
        items={[
          {
            id: 'item-1',
            objective: 'Cerrar brecha',
            progress: 40,
            justification: 'Retraso institucional',
          },
        ]}
        canEdit
        canEditProgressOnly
        isLoading={false}
        isError={false}
        isSaving={false}
        isImporting={false}
        onRefresh={vi.fn()}
        onImport={vi.fn()}
        onCreate={vi.fn()}
        onUpdate={vi.fn()}
      />,
    );

    expect(screen.getByText('Retraso institucional')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Importar desde FODA/i })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Editar/i })).toBeInTheDocument();
  });

  it('oculta acciones de escritura para lectura', () => {
    render(
      <ImprovementPlanPanel
        items={[{ id: 'item-1', objective: 'Solo lectura', progress: 0 }]}
        canEdit={false}
        isLoading={false}
        isError={false}
        isSaving={false}
        isImporting={false}
        onRefresh={vi.fn()}
        onImport={vi.fn()}
        onCreate={vi.fn()}
        onUpdate={vi.fn()}
      />,
    );

    expect(screen.queryByRole('button', { name: /Importar desde FODA/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Añadir ítem/i })).not.toBeInTheDocument();
  });
});
