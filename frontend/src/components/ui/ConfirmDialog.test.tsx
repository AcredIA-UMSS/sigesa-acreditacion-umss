import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ConfirmDialog } from './ConfirmDialog';

describe('ConfirmDialog', () => {
  it('shouldNotRenderWhenClosed', () => {
    render(
      <ConfirmDialog
        isOpen={false}
        title="Desactivar usuario"
        description="Esta acción revoca el acceso."
        onClose={vi.fn()}
        onConfirm={vi.fn()}
      />,
    );

    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
  });

  it('shouldConfirmAndCancelFromOpenDialog', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    const onConfirm = vi.fn();
    render(
      <ConfirmDialog
        isOpen
        title="Desactivar usuario"
        description="Esta acción revoca el acceso."
        onClose={onClose}
        onConfirm={onConfirm}
      />,
    );

    expect(screen.getByRole('alertdialog', { name: 'Desactivar usuario' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Confirmar' }));
    expect(onConfirm).toHaveBeenCalledOnce();
    await user.click(screen.getByRole('button', { name: 'Cancelar' }));
    expect(onClose).toHaveBeenCalled();
  });

  it('shouldDisableActionsWhileLoading', () => {
    render(
      <ConfirmDialog
        isOpen
        isLoading
        title="Desactivar usuario"
        description="Procesando"
        onClose={vi.fn()}
        onConfirm={vi.fn()}
      />,
    );

    expect(screen.getByRole('button', { name: 'Procesando…' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled();
  });
});
