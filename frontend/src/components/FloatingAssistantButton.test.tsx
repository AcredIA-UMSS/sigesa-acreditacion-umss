import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeAll, describe, expect, it } from 'vitest';
import { seedSession } from '../test/session';
import { renderWithProviders } from '../test/test-utils';
import { FloatingAssistantButton } from './FloatingAssistantButton';

beforeAll(() => {
  Element.prototype.scrollIntoView = () => undefined;
});

describe('FloatingAssistantButton', () => {
  it('shows the FAB on dashboard routes and opens an accessible dialog', async () => {
    seedSession('CC');
    const user = userEvent.setup();
    renderWithProviders(<FloatingAssistantButton />, { route: '/dashboard' });

    const trigger = screen.getByRole('button', { name: 'Abrir asistente' });
    expect(trigger).toBeInTheDocument();

    await user.click(trigger);

    const dialog = screen.getByRole('dialog', { name: 'Asistente virtual' });
    expect(dialog).toHaveAttribute('aria-modal', 'true');
    expect(within(dialog).getByRole('button', { name: 'Cerrar asistente' })).toBeInTheDocument();
  });

  it('closes with Escape and returns focus to the trigger', async () => {
    seedSession('JD');
    const user = userEvent.setup();
    renderWithProviders(<FloatingAssistantButton />, { route: '/procesos' });

    const trigger = screen.getByRole('button', { name: 'Abrir asistente' });
    await user.click(trigger);
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    await user.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(trigger).toHaveFocus();
  });

  it('is hidden on the full-page help route', () => {
    seedSession('TD');
    renderWithProviders(<FloatingAssistantButton />, { route: '/ayuda' });
    expect(screen.queryByRole('button', { name: 'Abrir asistente' })).not.toBeInTheDocument();
  });
});
