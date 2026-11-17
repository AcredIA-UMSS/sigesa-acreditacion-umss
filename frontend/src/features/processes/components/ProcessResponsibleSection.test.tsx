import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '../../../test/test-utils';
import { AssignResponsibleModalUI } from './ProcessResponsibleSection';

describe('AssignResponsibleModalUI', () => {
  it('lists only provided career coordinators as selectable cards', async () => {
    const user = userEvent.setup();
    const onSelectUser = vi.fn();

    renderWithProviders(
      <AssignResponsibleModalUI
        isOpen
        careerName="Ingeniería de Sistemas"
        candidates={[
          { userId: 'cc-1', fullName: 'Demo CC', email: 'cc@umss.edu.bo' },
        ]}
        selectedUserId=""
        isLoadingCandidates={false}
        isSubmitting={false}
        submitError={null}
        loadError={null}
        onClose={() => undefined}
        onSelectUser={onSelectUser}
        onSubmit={() => undefined}
      />,
    );

    expect(screen.getByText(/Ingeniería de Sistemas/)).toBeInTheDocument();
    expect(screen.getByText('Demo CC')).toBeInTheDocument();
    expect(screen.getByText('cc@umss.edu.bo')).toBeInTheDocument();

    await user.click(screen.getByRole('radio', { name: /Demo CC/ }));
    expect(onSelectUser).toHaveBeenCalledWith('cc-1');
  });

  it('explains empty list when the career has no coordinators', () => {
    renderWithProviders(
      <AssignResponsibleModalUI
        isOpen
        careerName="Arquitectura"
        candidates={[]}
        selectedUserId=""
        isLoadingCandidates={false}
        isSubmitting={false}
        submitError={null}
        loadError={null}
        onClose={() => undefined}
        onSelectUser={() => undefined}
        onSubmit={() => undefined}
      />,
    );

    expect(
      screen.getByText(/No hay coordinadores \[CC\] activos asignados a Arquitectura/),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /administración de usuarios/i })).toHaveAttribute(
      'href',
      '/admin/users',
    );
    expect(screen.getByRole('button', { name: /Confirmar asignación/ })).toBeDisabled();
  });
});
