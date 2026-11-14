import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { PeerRoomChecklistPanel } from './PeerRoomChecklistPanel';

describe('PeerRoomChecklistPanel', () => {
  it('permite a TD/EE marcar ítems del checklist', async () => {
    const onToggle = vi.fn();
    const user = userEvent.setup();
    render(
      <PeerRoomChecklistPanel
        items={[
          {
            itemCode: 'DOCUMENTATION_PACKAGE',
            label: 'Paquete documental',
            checked: false,
          },
        ]}
        canEdit
        isLoading={false}
        isSaving={false}
        onToggle={onToggle}
      />,
    );

    expect(screen.getByText('Checklist Sala de Pares')).toBeInTheDocument();
    await user.click(screen.getByLabelText('Paquete documental'));
    expect(onToggle).toHaveBeenCalledWith('DOCUMENTATION_PACKAGE', true);
  });

  it('deshabilita checkboxes en solo lectura', () => {
    render(
      <PeerRoomChecklistPanel
        items={[
          {
            itemCode: 'LOGISTICS',
            label: 'Logística',
            checked: true,
          },
        ]}
        canEdit={false}
        isLoading={false}
        isSaving={false}
        onToggle={vi.fn()}
      />,
    );

    expect(screen.getByLabelText('Logística')).toBeDisabled();
  });
});
