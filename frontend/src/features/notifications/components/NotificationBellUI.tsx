import {
  NotificationBellPopoverPresentation,
  type NotificationBellPopoverProps,
} from './NotificationBellPopover';
import type { NotificationItemView } from '../types';

export type NotificationBellUIProps = {
  open: boolean;
  unreadCount: number;
  items: NotificationItemView[];
  isLoading: boolean;
  isMarkingAll: boolean;
  errorMessage: string | null;
  compact?: boolean;
  variant?: NotificationBellPopoverProps['variant'];
  onToggle: () => void;
  onClose: () => void;
  onItemClick: (item: NotificationItemView) => void;
  onMarkAllRead: () => void;
};

/** Presentation-only alias kept for existing unit tests. */
export function NotificationBellUI({
  open,
  unreadCount,
  items,
  isLoading,
  isMarkingAll,
  errorMessage,
  compact = false,
  variant = 'sidebar',
  onToggle,
  onClose,
  onItemClick,
  onMarkAllRead,
}: NotificationBellUIProps) {
  return (
    <NotificationBellPopoverPresentation
      open={open}
      unreadCount={unreadCount}
      items={items}
      isLoading={isLoading}
      isMarkingAll={isMarkingAll}
      errorMessage={errorMessage}
      variant={variant}
      compact={compact}
      onToggle={onToggle}
      onClose={onClose}
      onItemClick={onItemClick}
      onMarkAllRead={onMarkAllRead}
    />
  );
}
