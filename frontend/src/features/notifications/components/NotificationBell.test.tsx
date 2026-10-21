import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NotificationBell } from './NotificationBell';
import { useNotifications } from '../hooks/useNotifications';

vi.mock('../hooks/useNotifications', () => ({
  useNotifications: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

describe('NotificationBell Component', () => {
  const markAsReadMock = vi.fn();
  const markAllAsReadMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders notification bell icon with unread badge count', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 4,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    const badge = screen.getByTestId('unread-badge');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveTextContent('4');
  });

  it('opens popup when clicked and displays notifications list', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'n-1',
          eventType: 'IndicatorRejected',
          recipientId: 'r-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Evidencia Observada',
          body: 'Se requiere subsanación',
          deepLink: '/procesos/p-1?indicadorId=i-1',
          status: 'SENT',
          createdAt: '2026-10-20T10:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    const button = screen.getByRole('button', { name: /notificaciones/i });
    fireEvent.click(button);

    expect(screen.getByText('Evidencia Observada')).toBeInTheDocument();
    expect(screen.getByText('Se requiere subsanación')).toBeInTheDocument();

    const notifItem = screen.getByText('Evidencia Observada');
    fireEvent.click(notifItem);

    expect(markAsReadMock).toHaveBeenCalledWith('n-1');
    expect(mockNavigate).toHaveBeenCalledWith('/procesos/p-1?indicadorId=i-1');
  });

  it('allows clicking mark all as read', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'n-1',
          eventType: 'IndicatorRejected',
          recipientId: 'r-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Evidencia Observada',
          body: 'Se requiere subsanación',
          deepLink: null,
          status: 'SENT',
          createdAt: '2026-10-20T10:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    const button = screen.getByRole('button', { name: /notificaciones/i });
    fireEvent.click(button);

    const markAllBtn = screen.getByRole('button', { name: /marcar todas/i });
    fireEvent.click(markAllBtn);

    expect(markAllAsReadMock).toHaveBeenCalledTimes(1);
  });

  it('navigates to the full notifications page via "Ver todas"', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'n-1',
          eventType: 'IndicatorApproved',
          recipientId: 'r-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Indicador Aprobado',
          body: 'El indicador fue aprobado',
          deepLink: null,
          status: 'SENT',
          createdAt: '2026-10-20T10:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));

    const viewAllBtn = screen.getByRole('button', { name: /ver todas las notificaciones/i });
    fireEvent.click(viewAllBtn);

    expect(mockNavigate).toHaveBeenCalledWith('/notificaciones');
  });

  it('hides the badge when there are no unread notifications', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    expect(screen.queryByTestId('unread-badge')).not.toBeInTheDocument();
  });

  it('caps the badge display at 99+ for large unread counts', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 150,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    expect(screen.getByTestId('unread-badge')).toHaveTextContent('99+');
  });

  it('shows the loading state while notifications are being fetched', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: true,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));

    expect(screen.getByText(/cargando notificaciones/i)).toBeInTheDocument();
  });

  it('shows the empty state when there are no pending notifications', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));

    expect(screen.getByText(/no tienes notificaciones pendientes/i)).toBeInTheDocument();
  });

  it('does not mark an already-read notification again when reopening it', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'n-read',
          eventType: 'IndicatorApproved',
          recipientId: 'r-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Indicador ya revisado',
          body: 'Sin acciones pendientes',
          deepLink: '/procesos/p-9',
          status: 'SENT',
          createdAt: '2026-10-18T10:00:00',
          readAt: '2026-10-18T11:00:00',
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 0,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));
    fireEvent.click(screen.getByText('Indicador ya revisado'));

    expect(markAsReadMock).not.toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/procesos/p-9');
  });

  it('closes the dropdown when clicking outside', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [],
      totalElements: 0,
      isLoading: false,
      isError: false,
      unreadCount: 2,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));
    expect(screen.getByRole('dialog', { name: /panel de notificaciones/i })).toBeInTheDocument();

    fireEvent.mouseDown(document.body);

    expect(screen.queryByRole('dialog', { name: /panel de notificaciones/i })).not.toBeInTheDocument();
  });

  it('disables the mark-all action while it is being processed', () => {
    vi.mocked(useNotifications).mockReturnValue({
      notifications: [
        {
          id: 'n-1',
          eventType: 'IndicatorRejected',
          recipientId: 'r-1',
          recipientEmail: 'cc@umss.edu.bo',
          subject: 'Evidencia Observada',
          body: 'Se requiere subsanación',
          deepLink: null,
          status: 'SENT',
          createdAt: '2026-10-20T10:00:00',
          readAt: null,
        },
      ],
      totalElements: 1,
      isLoading: false,
      isError: false,
      unreadCount: 1,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: true,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));

    expect(screen.getByRole('button', { name: /marcar todas/i })).toBeDisabled();
  });

  it('maps every supported event type to a coloured icon', () => {
    const eventTypes = [
      'IndicatorRejected',
      'IndicatorApproved',
      'DeadlineApproaching',
      'DeadlineWarning',
      'UnknownEvent',
    ];

    vi.mocked(useNotifications).mockReturnValue({
      notifications: eventTypes.map((eventType, index) => ({
        id: `n-${index}`,
        eventType,
        recipientId: 'r-1',
        recipientEmail: 'cc@umss.edu.bo',
        subject: `Evento ${eventType}`,
        body: 'Detalle del evento',
        deepLink: null,
        status: 'SENT',
        createdAt: '2026-10-20T10:00:00',
        readAt: null,
      })),
      totalElements: eventTypes.length,
      isLoading: false,
      isError: false,
      unreadCount: eventTypes.length,
      markAsRead: markAsReadMock,
      markAllAsRead: markAllAsReadMock,
      isMarkingAll: false,
      refetch: vi.fn(),
      totalPages: 1,
      page: 0,
      setPage: vi.fn(),
    });

    const { container } = render(<NotificationBell />);

    fireEvent.click(screen.getByRole('button', { name: /notificaciones/i }));

    expect(container.querySelectorAll('svg.text-secondary').length).toBeGreaterThan(0);
    expect(container.querySelectorAll('svg.text-success').length).toBeGreaterThan(0);
    expect(container.querySelectorAll('svg.text-warning').length).toBeGreaterThan(0);
    expect(container.querySelectorAll('svg.text-primary-500').length).toBeGreaterThan(0);
  });
});
