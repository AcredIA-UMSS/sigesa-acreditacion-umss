import { customFetch } from '../../../lib/api/customFetch';

export interface NotificationItemDto {
  id: string;
  eventType: string;
  recipientId: string | null;
  recipientEmail: string | null;
  subject: string;
  body: string;
  deepLink: string | null;
  status: string;
  createdAt: string;
  readAt: string | null;
}

export interface PaginatedNotificationResponse {
  content: NotificationItemDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface NotificationQueryParams {
  unreadOnly?: boolean;
  page?: number;
  size?: number;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export async function fetchMyNotifications(
  params: NotificationQueryParams = {},
  signal?: AbortSignal,
): Promise<PaginatedNotificationResponse> {
  const query = new URLSearchParams();
  if (params.unreadOnly !== undefined) {
    query.set('unreadOnly', String(params.unreadOnly));
  }
  query.set('page', String(params.page ?? 0));
  query.set('size', String(params.size ?? 20));

  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  const res = await customFetch<{ data: PaginatedNotificationResponse }>(
    `/api/v1/notifications/me?${query.toString()}`,
    {
      method: 'GET',
      signal: timeoutSignal,
    },
  );
  return res.data;
}

export async function fetchUnreadCount(signal?: AbortSignal): Promise<number> {
  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  const res = await customFetch<{ data: UnreadCountResponse }>(
    `/api/v1/notifications/unread-count`,
    {
      method: 'GET',
      signal: timeoutSignal,
    },
  );
  return res.data.unreadCount;
}

export async function markNotificationAsRead(
  id: string,
  signal?: AbortSignal,
): Promise<void> {
  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  await customFetch<void>(`/api/v1/notifications/${id}/read`, {
    method: 'PATCH',
    signal: timeoutSignal,
  });
}

export async function markAllNotificationsAsRead(
  signal?: AbortSignal,
): Promise<void> {
  const timeoutSignal = signal ?? AbortSignal.timeout(10000);

  await customFetch<void>(`/api/v1/notifications/mark-all-read`, {
    method: 'POST',
    signal: timeoutSignal,
  });
}
