import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Bell } from 'lucide-react';
import type { NotificationItemView } from '../types';
import { useNotificationInbox } from '../hooks/useNotificationInbox';

export type NotificationBellPopoverProps = {
  /** Dark sidebar chrome vs light page header. */
  variant?: 'sidebar' | 'header';
  compact?: boolean;
};

type PresentationProps = {
  open: boolean;
  unreadCount: number;
  items: NotificationItemView[];
  isLoading: boolean;
  isMarkingAll: boolean;
  errorMessage: string | null;
  variant: 'sidebar' | 'header';
  compact: boolean;
  onToggle: () => void;
  onClose: () => void;
  onItemClick: (item: NotificationItemView) => void;
  onMarkAllRead: () => void;
};

type PanelCoords = {
  top: number;
  left: number;
  maxHeight: number;
  width: number;
};

const PANEL_WIDTH = 320;
const VIEWPORT_PAD = 12;
const GAP = 8;

function clampPanel(trigger: DOMRect, preferred: 'up' | 'down'): PanelCoords {
  const vw = window.innerWidth;
  const vh = window.innerHeight;
  const width = Math.min(PANEL_WIDTH, vw - VIEWPORT_PAD * 2);

  // Header (down): align to trigger right edge so panel opens into content.
  // Sidebar (up): align to trigger left edge so panel opens into content.
  let left = preferred === 'up' ? trigger.left : trigger.right - width;
  left = Math.max(VIEWPORT_PAD, Math.min(left, vw - width - VIEWPORT_PAD));

  const spaceBelow = vh - trigger.bottom - VIEWPORT_PAD - GAP;
  const spaceAbove = trigger.top - VIEWPORT_PAD - GAP;
  const openDown =
    preferred === 'down'
      ? spaceBelow >= 160 || spaceBelow >= spaceAbove
      : spaceAbove < 160 && spaceBelow > spaceAbove;

  let top: number;
  let maxHeight: number;
  if (openDown) {
    top = trigger.bottom + GAP;
    maxHeight = Math.max(160, Math.min(384, vh - top - VIEWPORT_PAD));
  } else {
    maxHeight = Math.max(160, Math.min(384, spaceAbove));
    top = trigger.top - GAP - maxHeight;
    if (top < VIEWPORT_PAD) {
      top = VIEWPORT_PAD;
      maxHeight = Math.max(160, trigger.top - VIEWPORT_PAD - GAP);
    }
  }

  return { top, left, maxHeight, width };
}

function NotificationBellPopoverPresentation({
  open,
  unreadCount,
  items,
  isLoading,
  isMarkingAll,
  errorMessage,
  variant,
  compact,
  onToggle,
  onClose,
  onItemClick,
  onMarkAllRead,
}: PresentationProps) {
  const badgeLabel = unreadCount > 99 ? '99+' : String(unreadCount);
  const isSidebar = variant === 'sidebar';
  const triggerRef = useRef<HTMLButtonElement>(null);
  const [coords, setCoords] = useState<PanelCoords | null>(null);

  const triggerClass = isSidebar
    ? `relative inline-flex shrink-0 items-center justify-center rounded-lg p-2 text-primary-200 transition-colors hover:bg-primary-800 hover:text-body ${
        open ? 'bg-primary-800 text-body' : ''
      }`
    : `relative inline-flex shrink-0 items-center justify-center rounded-lg border border-gray-200 bg-body p-2 text-gray-700 shadow-sm transition-colors hover:bg-gray-50 hover:text-primary ${
        open ? 'border-primary-200 bg-primary-50 text-primary' : ''
      }`;

  const reposition = () => {
    const el = triggerRef.current;
    if (!el) {
      return;
    }
    setCoords(clampPanel(el.getBoundingClientRect(), isSidebar ? 'up' : 'down'));
  };

  useLayoutEffect(() => {
    if (!open) {
      setCoords(null);
      return;
    }
    reposition();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reposition closes over latest trigger/variant
  }, [open, isSidebar, items.length, unreadCount]);

  useEffect(() => {
    if (!open) {
      return;
    }
    const onWin = () => reposition();
    window.addEventListener('resize', onWin);
    window.addEventListener('scroll', onWin, true);
    return () => {
      window.removeEventListener('resize', onWin);
      window.removeEventListener('scroll', onWin, true);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, isSidebar]);

  const panel =
    open && coords
      ? createPortal(
          <>
            <button
              type="button"
              className="fixed inset-0 z-[60] cursor-default bg-transparent"
              aria-label="Cerrar notificaciones"
              onClick={onClose}
            />
            <div
              role="dialog"
              aria-label="Notificaciones recientes"
              style={{
                position: 'fixed',
                top: coords.top,
                left: coords.left,
                width: coords.width,
                maxHeight: coords.maxHeight,
              }}
              className="z-[70] flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-body shadow-lg"
            >
              <div className="flex shrink-0 items-center justify-between gap-2 border-b border-gray-200 px-3 py-2">
                <p className="text-label-md font-medium text-primary-900">Notificaciones</p>
                <button
                  type="button"
                  onClick={onMarkAllRead}
                  disabled={isMarkingAll || unreadCount === 0}
                  className="text-label-md font-medium text-primary disabled:cursor-not-allowed disabled:text-gray-400 hover:underline"
                >
                  {isMarkingAll ? 'Marcando…' : 'Marcar leídas'}
                </button>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto">
                {isLoading && <p className="px-3 py-4 text-body-md text-gray-600">Cargando…</p>}
                {errorMessage && !isLoading && (
                  <p className="px-3 py-4 text-body-md text-secondary">{errorMessage}</p>
                )}
                {!isLoading && !errorMessage && items.length === 0 && (
                  <p className="px-3 py-4 text-body-md text-gray-600">No hay notificaciones recientes.</p>
                )}
                {!isLoading && items.length > 0 && (
                  <ul className="divide-y divide-gray-100">
                    {items.map((item) => {
                      const hasDeepLink = Boolean(item.deepLink);
                      return (
                      <li key={item.id}>
                        <button
                          type="button"
                          onClick={() => onItemClick(item)}
                          aria-label={
                            hasDeepLink
                              ? `${item.title}. Abrir detalle.`
                              : `${item.title}. Marcar como leída.`
                          }
                          className={`flex w-full items-start gap-2 px-3 py-2.5 text-left transition-colors hover:bg-gray-50 ${
                            item.read ? 'bg-body' : 'bg-primary-50'
                          }`}
                        >
                          <span
                            className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                              item.read ? 'bg-transparent' : 'bg-secondary'
                            }`}
                            aria-hidden
                          />
                          <span className="flex min-w-0 flex-col gap-1">
                            <span className="line-clamp-1 text-body-md font-semibold text-primary-900">
                              {item.title}
                            </span>
                            <span className="line-clamp-2 text-body-md text-gray-700">{item.body}</span>
                          </span>
                        </button>
                      </li>
                      );
                    })}
                  </ul>
                )}
              </div>
            </div>
          </>,
          document.body,
        )
      : null;

  return (
    <div className="relative shrink-0">
      <button
        ref={triggerRef}
        type="button"
        onClick={onToggle}
        className={triggerClass}
        aria-label={unreadCount > 0 ? `Notificaciones (${unreadCount} sin leer)` : 'Notificaciones'}
        aria-expanded={open}
        aria-haspopup="dialog"
      >
        <Bell size={compact ? 18 : 20} />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 inline-flex min-w-4 items-center justify-center rounded-full bg-secondary px-1 text-label-md font-medium text-body">
            {badgeLabel}
          </span>
        )}
      </button>
      {panel}
    </div>
  );
}

/** Drop-in bell + short popover wired to Orval notification endpoints. */
export function NotificationBellPopover({
  variant = 'header',
  compact = false,
}: NotificationBellPopoverProps) {
  const inbox = useNotificationInbox();

  return (
    <NotificationBellPopoverPresentation
      open={inbox.open}
      unreadCount={inbox.unreadCount}
      items={inbox.items}
      isLoading={inbox.isLoading}
      isMarkingAll={inbox.isMarkingAll}
      errorMessage={inbox.errorMessage}
      variant={variant}
      compact={compact}
      onToggle={inbox.onToggle}
      onClose={inbox.onClose}
      onItemClick={(item) => {
        void inbox.onItemClick(item);
      }}
      onMarkAllRead={() => {
        void inbox.onMarkAllRead();
      }}
    />
  );
}

export { NotificationBellPopoverPresentation };
