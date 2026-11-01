import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ProcessFreezePanel } from './ProcessFreezePanel';

describe('ProcessFreezePanel', () => {
  it('permite congelar cuando TD y no hay freeze activo', async () => {
    const onFreeze = vi.fn();
    const user = userEvent.setup();
    render(
      <ProcessFreezePanel
        freeze={{ active: false }}
        canFreeze
        canLift
        isLoading={false}
        isMutating={false}
        onFreeze={onFreeze}
        onLift={vi.fn()}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Congelar expediente/i }));
    expect(onFreeze).toHaveBeenCalled();
  });

  it('ofrece levantar a operativo o monitoreo cuando el freeze está activo', async () => {
    const onLift = vi.fn();
    const user = userEvent.setup();
    render(
      <ProcessFreezePanel
        freeze={{ active: true, mode: 'AUDIT_READONLY' }}
        canFreeze
        canLift
        isLoading={false}
        isMutating={false}
        onFreeze={vi.fn()}
        onLift={onLift}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Levantar a operativo/i }));
    expect(onLift).toHaveBeenCalledWith('ACTIVE');
    await user.click(screen.getByRole('button', { name: /Levantar a monitoreo/i }));
    expect(onLift).toHaveBeenCalledWith('IMPROVEMENT_MONITORING');
  });
});
