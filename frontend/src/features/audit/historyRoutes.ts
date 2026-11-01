export interface AuditHistoryView {
  id: 'general' | 'access' | 'evidence' | 'accounts' | 'user';
  path: string;
  menuLabel: string;
  title: string;
  description: string;
  testId: string;
}

export const AUDIT_HISTORY_VIEWS: readonly AuditHistoryView[] = [
  {
    id: 'general',
    path: '/admin/historico',
    menuLabel: 'Historial general',
    title: 'Historial general',
    description: 'Todas las acciones registradas en la bitácora.',
    testId: 'sidebar-nav-history-general',
  },
  {
    id: 'access',
    path: '/admin/historico/accesos',
    menuLabel: 'Historial de accesos',
    title: 'Historial de accesos',
    description: 'Inicios de sesión, cierres de sesión y sesiones expiradas, con IP y resultado cuando el registro los incluye.',
    testId: 'sidebar-nav-history-access',
  },
  {
    id: 'evidence',
    path: '/admin/historico/evidencias',
    menuLabel: 'Historial de evidencias',
    title: 'Historial de evidencias',
    description: 'Cargas de evidencia registradas, con archivo, indicador y proceso cuando la carga los guardó.',
    testId: 'sidebar-nav-history-evidence',
  },
  {
    id: 'accounts',
    path: '/admin/historico/cuentas',
    menuLabel: 'Cuentas y reportes',
    title: 'Cuentas y reportes',
    description: 'Altas, cambios de estado de cuenta y solicitudes del reporte ejecutivo.',
    testId: 'sidebar-nav-history-accounts',
  },
  {
    id: 'user',
    path: '/admin/historico/usuario',
    menuLabel: 'Historial por usuario',
    title: 'Historial por usuario',
    description: 'Actividad de un usuario, en tabla o en línea de tiempo.',
    testId: 'sidebar-nav-history-user',
  },
];

export function findAuditHistoryView(pathname: string): AuditHistoryView | undefined {
  return AUDIT_HISTORY_VIEWS.find((view) => view.path === pathname);
}
