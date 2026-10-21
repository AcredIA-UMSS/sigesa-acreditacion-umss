import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { CoordinatorAlertsPanel } from './CoordinatorAlertsPanel';
import { useNotifications } from '../hooks/useNotifications';

vi.mock('../hooks/useNotifications', () => ({
  useNotifications: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

describe('CoordinatorAlertsPanel Component', () => {
  const markAsReadMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders empty state when there are no alerts', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    expect(screen.getByText(/alertas y notificaciones activas/i)).toBeInTheDocument();
    expect(screen.getByText(/no tienes alertas ni observaciones críticas/i)).toBeInTheDocument();
  });

  it('renders alert items and navigates on click', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'alert-1',
          eventType: 'IndicatorRejected',
          recipientId: 'prog-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Evidencia 1.2 Rechazada',
          body: 'Se requiere adjuntar documento firmado',
          deepLink: '/procesos/p-1?indicadorId=ind-1',
          status: 'SENT',
          createdAt: '2026-10-20T11:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    expect(screen.getByText('Evidencia 1.2 Rechazada')).toBeInTheDocument();
    expect(screen.getByText('Se requiere adjuntar documento firmado')).toBeInTheDocument();
    expect(screen.getByText('1 pendientes')).toBeInTheDocument();

    const item = screen.getByText('Evidencia 1.2 Rechazada');
    fireEvent.click(item);

    expect(markAsReadMock).toHaveBeenCalledWith('alert-1');
    expect(mockNavigate).toHaveBeenCalledWith('/procesos/p-1?indicadorId=ind-1');
  });

  it('shows the loading state while alerts are being fetched', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: true,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    expect(screen.getByText(/cargando alertas/i)).toBeInTheDocument();
  });

  it('marks the alert as read without navigating when there is no deep link', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'alert-nodeeplink',
          eventType: 'DeadlineApproaching',
          recipientId: 'prog-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Plazo próximo',
          body: 'Vence en 3 días',
          deepLink: null,
          status: 'SENT',
          createdAt: '2026-10-20T11:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    fireEvent.click(screen.getByText('Plazo próximo'));

    expect(markAsReadMock).toHaveBeenCalledWith('alert-nodeeplink');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('hides the pending counter when there are no unread alerts', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    expect(screen.queryByText(/^\d+ pendientes$/)).not.toBeInTheDocument();
  });

  it('requests only unread alerts for the dashboard panel', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: vi.fn(),
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<CoordinatorAlertsPanel />);

    expect(vi.mocked(useNotifications)).toHaveBeenCalledWith({ unreadOnly: true, size: 5 });
  });
});
