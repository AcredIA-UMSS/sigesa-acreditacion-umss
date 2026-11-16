import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '../../../test/test-utils';
import { PeerVisitAgendaView } from './PeerVisitAgendaView';

describe('PeerVisitAgendaView', () => {
  it('lets technician save venue, room and a document item', async () => {
    const onSave = vi.fn();
    const onChange = vi.fn();
    renderWithProviders(
      <PeerVisitAgendaView
        processId="proc-1"
        canEdit
        isLoading={false}
        isSaving={false}
        errorMessage={null}
        onChange={onChange}
        onItemChange={vi.fn()}
        onAddItem={vi.fn()}
        onRemoveItem={vi.fn()}
        onSave={onSave}
        view={{ processId: 'proc-1', careerName: 'Ingeniería de Sistemas', visit: null }}
        draft={{
          startsOn: '2026-11-24',
          endsOn: '2026-11-28',
          venue: 'Campus Central UMSS',
          room: 'Sala de Pares 3',
          status: 'SCHEDULED',
          notes: '',
          items: [{ type: 'DOCUMENT', title: 'Informe de autoevaluación' }],
        }}
      />,
      { route: '/procesos/proc-1/visita-pares' },
    );

    expect(screen.getByRole('heading', { name: 'Agenda de visita de pares' })).toBeInTheDocument();
    expect(screen.getByDisplayValue('Campus Central UMSS')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Sala de Pares 3')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Informe de autoevaluación')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /Guardar agenda/i }));
    expect(onSave).toHaveBeenCalledOnce();
  });
});
