import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  CheckCheck,
  AlertTriangle,
  CheckCircle2,
  Clock,
  ExternalLink,
  Info,
} from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';
import type { NotificationItemDto } from '../api/notificationsApi';

interface NotificationBellProps {
  className?: string;
}

export function NotificationBell({ className = '' }: NotificationBellProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const {
    notifications,
    unreadCount,
    isLoading,
    markAsRead,
    markAllAsRead,
    isMarkingAll,
  } = useNotifications({ size: 10 });

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    }
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const handleNotificationClick = (notification: NotificationItemDto) => {
    if (!notification.readAt) {
      markAsRead(notification.id);
    }
    setIsOpen(false);

    if (notification.deepLink) {
      navigate(notification.deepLink);
    }
  };

  const getEventIcon = (eventType: string) => {
    switch (eventType) {
      case 'IndicatorRejected':
        return <AlertTriangle className="text-secondary shrink-0" size={18} />;
      case 'IndicatorApproved':
        return <CheckCircle2 className="text-success shrink-0" size={18} />;
      case 'DeadlineApproaching':
      case 'DeadlineWarning':
        return <Clock className="text-warning shrink-0" size={18} />;
      default:
        return <Info className="text-primary-500 shrink-0" size={18} />;
    }
  };

  return (
    <div className={`relative inline-block ${className}`} ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        className="relative flex h-10 w-10 items-center justify-center rounded-xl border border-primary-700/60 bg-primary-800/80 text-primary-100 hover:bg-primary-700 hover:text-white transition-colors focus:outline-none focus:ring-2 focus:ring-secondary-400"
        aria-label="Notificaciones"
        aria-expanded={isOpen}
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span
            data-testid="unread-badge"
            className="absolute -top-1.5 -right-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-secondary px-1 text-[11px] font-bold text-white shadow-sm ring-2 ring-primary-900"
          >
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div
          role="dialog"
          aria-label="Panel de notificaciones"
          className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl border border-primary-200/60 bg-body p-4 shadow-xl z-50 text-gray-800 animate-fadeIn"
        >
          <div className="flex items-center justify-between pb-3 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <h3 className="text-body-md font-bold text-primary-900">
                Notificaciones
              </h3>
              {unreadCount > 0 && (
                <span className="rounded-full bg-secondary-100 px-2 py-0.5 text-[11px] font-semibold text-secondary-800">
                  {unreadCount} nuevas
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={() => markAllAsRead()}
                disabled={isMarkingAll}
                className="flex items-center gap-1 text-xs font-semibold text-primary-600 hover:text-primary-800 hover:underline disabled:opacity-50"
              >
                <CheckCheck size={14} />
                Marcar todas
              </button>
            )}
          </div>

          <div className="mt-2 max-h-80 overflow-y-auto divide-y divide-gray-100">
            {isLoading ? (
              <div className="py-8 text-center text-xs text-gray-500">
                Cargando notificaciones...
              </div>
            ) : notifications.length === 0 ? (
              <div className="py-8 text-center text-xs text-gray-500">
                No tienes notificaciones pendientes.
              </div>
            ) : (
              notifications.map((item) => {
                const isUnread = !item.readAt;
                return (
                  <div
                    key={item.id}
                    onClick={() => handleNotificationClick(item)}
                    className={`flex items-start gap-3 p-3 rounded-xl cursor-pointer transition-colors ${
                      isUnread
                        ? 'bg-primary-50/60 hover:bg-primary-100/60'
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    <div className="mt-0.5">{getEventIcon(item.eventType)}</div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <p
                          className={`text-xs truncate ${
                            isUnread
                              ? 'font-bold text-primary-900'
                              : 'font-medium text-gray-800'
                          }`}
                        >
                          {item.subject}
                        </p>
                        {isUnread && (
                          <span className="h-2 w-2 rounded-full bg-secondary shrink-0" />
                        )}
                      </div>
                      <p className="mt-1 text-xs text-gray-600 line-clamp-2">
                        {item.body}
                      </p>
                      {item.deepLink && (
                        <div className="mt-1 flex items-center gap-1 text-[11px] text-primary-600 font-medium">
                          <span>Ver detalle</span>
                          <ExternalLink size={12} />
                        </div>
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <button
            type="button"
            onClick={() => {
              setIsOpen(false);
              navigate('/notificaciones');
            }}
            className="mt-3 w-full rounded-xl border-t border-gray-100 pt-3 text-center text-xs font-semibold text-primary-600 hover:text-primary-800 hover:underline"
          >
            Ver todas las notificaciones
          </button>
        </div>
      )}
    </div>
  );
}
