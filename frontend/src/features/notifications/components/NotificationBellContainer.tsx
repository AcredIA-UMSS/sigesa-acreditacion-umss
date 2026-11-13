import { NotificationBellPopover } from './NotificationBellPopover';

type NotificationBellContainerProps = {
  compact?: boolean;
};

/** Sidebar-oriented wrapper around {@link NotificationBellPopover}. */
export function NotificationBellContainer({ compact = false }: NotificationBellContainerProps) {
  return <NotificationBellPopover variant="sidebar" compact={compact} />;
}
