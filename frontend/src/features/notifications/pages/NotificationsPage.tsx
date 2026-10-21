import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BellRing,
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  AlertTriangle,
  CheckCircle2,
  Clock,
  ExternalLink,
  Info,
  XCircle,
} from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { useNotifications } from '../hooks/useNotifications';
import type { NotificationItemDto } from '../api/notificationsApi';

const PAGE_SIZE = 20;

function getEventIcon(eventType: string) {
  switch (eventType) {
    case 'IndicatorRejected':
      return <AlertTriangle className="text-secondary shrink-0" size={20} />;
    case 'IndicatorApproved':
      return <CheckCircle2 className="text-success shrink-0" size={20} />;
    case 'DeadlineApproaching':
    case 'DeadlineWarning':
      return <Clock className="text-warning shrink-0" size={20} />;
    default:
      return <Info className="text-primary-500 shrink-0" size={20} />;
  }
}

export function NotificationsPage() {
  const navigate = useNavigate();
  const [unreadOnly, setUnreadOnly] = useState(false);

  const {
    notifications,
    totalElements,
    totalPages,
    page,
    setPage,
    isLoading,
    isError,
    refetch,
    unreadCount,
    markAsRead,
    markAllAsRead,
    isMarkingAll,
  } = useNotifications({ unreadOnly, size: PAGE_SIZE });

  const handleOpenNotification = (notification: NotificationItemDto) => {
    if (!notification.readAt) {
      markAsRead(notification.id);
    }
    if (notification.deepLink) {
      navigate(notification.deepLink);
    }
  };

  const handleToggleFilter = () => {
    setPage(0);
    setUnreadOnly((prev) => !prev);
  };

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-primary-900/10">
      <Sidebar activeNav="notifications" />

      <main className="flex-1 overflow-y-auto px-8 py-8 text-primary-900 bg-gray-50/50">
        {/* Header */}
        <header className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div>
            <h1 className="text-heading-xl font-bold tracking-tight text-primary-800 flex items-center gap-3">
              <BellRing className="text-secondary" size={28} />
              Notificaciones
            </h1>
            <p className="mt-1 text-body-lg text-gray-700">
              Alertas y notificaciones transaccionales de su carrera · SIGESA
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {unreadCount > 0 && (
              <span className="rounded-full bg-secondary-100 px-3 py-1.5 text-label-md font-semibold text-secondary-800">
                {unreadCount} sin leer
              </span>
            )}
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={() => markAllAsRead()}
                disabled={isMarkingAll}
                className="flex items-center gap-2 rounded-xl border border-primary-200/60 bg-body px-4 py-2.5 text-label-md font-semibold text-primary-700 shadow-sm transition-colors hover:bg-primary-50 disabled:opacity-50"
              >
                <CheckCheck size={16} />
                Marcar todas como leídas
              </button>
            )}
            <button
              type="button"
              onClick={handleToggleFilter}
              className={`rounded-xl border px-4 py-2.5 text-label-md font-semibold shadow-sm transition-colors ${
                unreadOnly
                  ? 'border-secondary bg-secondary text-white'
                  : 'border-primary-200/60 bg-body text-primary-700 hover:bg-primary-50'
              }`}
              aria-pressed={unreadOnly}
            >
              {unreadOnly ? 'Solo no leídas' : 'Ver solo no leídas'}
            </button>
          </div>
        </header>

        {/* Error State */}
        {isError && !isLoading && (
          <div className="mx-auto max-w-xl rounded-2xl border border-secondary-500/20 bg-secondary-50 p-6 text-center text-secondary-900 shadow-sm">
            <XCircle className="mx-auto mb-3 text-secondary-500" size={40} />
            <h2 className="text-heading-md font-bold">Error al cargar notificaciones</h2>
            <p className="mt-1 text-body-md text-secondary-700">
              No se pudieron recuperar sus notificaciones. Intente nuevamente.
            </p>
            <button
              type="button"
              onClick={() => refetch()}
              className="mt-5 rounded-xl bg-secondary px-5 py-2.5 text-label-md font-semibold text-white transition-colors hover:bg-secondary-600"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Loading Skeleton */}
        {isLoading && !isError && <NotificationsSkeleton />}

        {/* Content */}
        {!isLoading && !isError && (
          <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
            {notifications.length === 0 ? (
              <div className="flex h-64 flex-col items-center justify-center text-gray-500">
                <CheckCircle2 className="text-success mb-3" size={36} />
                <p className="text-body-md font-medium">
                  {unreadOnly
                    ? 'No tiene notificaciones sin leer.'
                    : 'No tiene notificaciones registradas.'}
                </p>
              </div>
            ) : (
              <>
                <ul className="divide-y divide-gray-100">
                  {notifications.map((item) => {
                    const isUnread = !item.readAt;
                    return (
                      <li key={item.id}>
                        <button
                          type="button"
                          onClick={() => handleOpenNotification(item)}
                          className={`flex w-full items-start gap-4 rounded-xl p-4 text-left transition-colors ${
                            isUnread
                              ? 'bg-primary-50/60 hover:bg-primary-100/60'
                              : 'hover:bg-gray-50'
                          }`}
                        >
                          <div className="mt-0.5">{getEventIcon(item.eventType)}</div>
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-2">
                              <p
                                className={`truncate text-body-md ${
                                  isUnread
                                    ? 'font-bold text-primary-900'
                                    : 'font-medium text-gray-800'
                                }`}
                              >
                                {item.subject}
                              </p>
                              {isUnread && (
                                <span
                                  data-testid={`unread-dot-${item.id}`}
                                  className="h-2 w-2 shrink-0 rounded-full bg-secondary"
                                />
                              )}
                            </div>
                            <p className="mt-1 text-body-sm text-gray-600">{item.body}</p>
                            <div className="mt-1 flex items-center gap-3 text-[11px] text-gray-500">
                              <span>
                                {new Date(item.createdAt).toLocaleString('es-BO', {
                                  dateStyle: 'medium',
                                  timeStyle: 'short',
                                })}
                              </span>
                              {item.deepLink && (
                                <span className="flex items-center gap-1 font-medium text-primary-600">
                                  Ver detalle
                                  <ExternalLink size={12} />
                                </span>
                              )}
                            </div>
                          </div>
                        </button>
                      </li>
                    );
                  })}
                </ul>

                {/* Pagination */}
                {totalElements > PAGE_SIZE && (
                  <div className="mt-4 flex items-center justify-between border-t border-gray-100 pt-4">
                    <button
                      type="button"
                      onClick={() => setPage(Math.max(0, page - 1))}
                      disabled={page === 0}
                      className="flex items-center gap-1 rounded-xl border border-primary-200/60 px-3 py-2 text-label-md font-semibold text-primary-700 transition-colors hover:bg-primary-50 disabled:opacity-40 disabled:hover:bg-transparent"
                    >
                      <ChevronLeft size={16} />
                      Anterior
                    </button>
                    <span className="text-body-sm text-gray-600">
                      Página {page + 1} de {totalPages} · {totalElements} en total
                    </span>
                    <button
                      type="button"
                      onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                      disabled={page >= totalPages - 1}
                      className="flex items-center gap-1 rounded-xl border border-primary-200/60 px-3 py-2 text-label-md font-semibold text-primary-700 transition-colors hover:bg-primary-50 disabled:opacity-40 disabled:hover:bg-transparent"
                    >
                      Siguiente
                      <ChevronRight size={16} />
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

function NotificationsSkeleton() {
  return (
    <div className="space-y-3 animate-pulse rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
      {[...Array(5)].map((_, i) => (
        <div key={i} className="flex items-start gap-4 rounded-xl p-4">
          <div className="h-5 w-5 rounded-full bg-gray-200" />
          <div className="flex-1 space-y-2">
            <div className="h-4 w-1/3 rounded bg-gray-200" />
            <div className="h-3 w-2/3 rounded bg-gray-100" />
          </div>
        </div>
      ))}
    </div>
  );
}
