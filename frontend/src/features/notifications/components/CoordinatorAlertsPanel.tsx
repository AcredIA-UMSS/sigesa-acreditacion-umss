import { useNavigate } from 'react-router-dom';
import { AlertCircle, Clock, CheckCircle2, ChevronRight, BellRing } from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';

export function CoordinatorAlertsPanel() {
  const navigate = useNavigate();
  const { notifications, isLoading, markAsRead } = useNotifications({
    unreadOnly: true,
    size: 5,
  });

  const handleOpenAlert = (id: string, deepLink: string | null) => {
    markAsRead(id);
    if (deepLink) {
      navigate(deepLink);
    }
  };

  return (
    <div className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <BellRing className="text-secondary shrink-0" size={20} />
          <h3 className="text-heading-sm font-bold text-primary-800">
            Alertas y Notificaciones Activas
          </h3>
        </div>
        {notifications.length > 0 && (
          <span className="rounded-full bg-secondary/10 px-2 py-0.5 text-xs font-semibold text-secondary-800">
            {notifications.length} pendientes
          </span>
        )}
      </div>

      {isLoading ? (
        <div className="flex h-32 items-center justify-center text-xs text-gray-500">
          Cargando alertas...
        </div>
      ) : notifications.length === 0 ? (
        <div className="flex h-32 flex-col items-center justify-center text-gray-500">
          <CheckCircle2 className="text-success mb-2" size={28} />
          <span className="text-body-sm">No tienes alertas ni observaciones críticas pendientes.</span>
        </div>
      ) : (
        <div className="space-y-3 max-h-[220px] overflow-y-auto pr-1">
          {notifications.map((notif) => {
            const isRejection = notif.eventType === 'IndicatorRejected';
            const isDeadline = notif.eventType.includes('Deadline');

            return (
              <div
                key={notif.id}
                onClick={() => handleOpenAlert(notif.id, notif.deepLink)}
                className="group flex cursor-pointer items-start justify-between gap-3 rounded-xl border border-primary-100 bg-primary-50/40 p-3 hover:border-secondary-300 hover:bg-secondary-50/40 transition-all"
              >
                <div className="flex items-start gap-2.5">
                  <div className="mt-0.5">
                    {isRejection ? (
                      <AlertCircle className="text-secondary shrink-0" size={18} />
                    ) : isDeadline ? (
                      <Clock className="text-warning shrink-0" size={18} />
                    ) : (
                      <BellRing className="text-primary-500 shrink-0" size={18} />
                    )}
                  </div>
                  <div>
                    <h4 className="text-body-sm font-bold text-primary-900 group-hover:text-secondary-800">
                      {notif.subject}
                    </h4>
                    <p className="text-xs text-gray-600 line-clamp-1 mt-0.5">
                      {notif.body}
                    </p>
                  </div>
                </div>

                <div className="flex items-center text-primary-400 group-hover:text-secondary-600">
                  <ChevronRight size={16} />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
