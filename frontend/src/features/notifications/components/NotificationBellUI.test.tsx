import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { NotificationBellUI } from './NotificationBellUI';
import type { NotificationItemView } from '../types';

const unreadItem: NotificationItemView = {
  id: 'notif-1',
  title: 'Proceso cerrado',
  body: 'La carrera Sistemas cerró acreditación.',
  deepLink: '/executive/semaphore',
  read: false,
  createdAt: '2026-11-12T10:00:00',
  eventType: 'ProcessClosed',
};

describe('NotificationBellUI', () => {
  it('marks item read and navigates via handlers when an unread item is clicked', async () => {
    const user = userEvent.setup();
    const onItemClick = vi.fn();
    const onToggle = vi.fn();
    const onClose = vi.fn();
    const onMarkAllRead = vi.fn();

    render(
      <NotificationBellUI
        open
        unreadCount={1}
        items={[unreadItem]}
        isLoading={false}
        isMarkingAll={false}
        errorMessage={null}
        onToggle={onToggle}
        onClose={onClose}
        onItemClick={onItemClick}
        onMarkAllRead={onMarkAllRead}
      />,
    );

    expect(screen.getByRole('dialog', { name: 'Notificaciones recientes' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Proceso cerrado/ }));
    expect(onItemClick).toHaveBeenCalledOnce();
    expect(onItemClick).toHaveBeenCalledWith(unreadItem);
  });

  it('exposes mark-all-read action when there are unread items', async () => {
    const user = userEvent.setup();
    const onMarkAllRead = vi.fn();

    render(
      <NotificationBellUI
        open
        unreadCount={2}
        items={[unreadItem]}
        isLoading={false}
        isMarkingAll={false}
        errorMessage={null}
        onToggle={() => undefined}
        onClose={() => undefined}
        onItemClick={() => undefined}
        onMarkAllRead={onMarkAllRead}
      />,
    );

    await user.click(screen.getByRole('button', { name: 'Marcar leídas' }));
    expect(onMarkAllRead).toHaveBeenCalledOnce();
  });
});
