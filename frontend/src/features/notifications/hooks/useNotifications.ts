import { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchMyNotifications,
  fetchUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  type NotificationQueryParams,
} from '../api/notificationsApi';

export const NOTIFICATIONS_QUERY_KEY = ['notifications', 'me'] as const;
export const UNREAD_COUNT_QUERY_KEY = ['notifications', 'unread-count'] as const;

const DEFAULT_PAGE_SIZE = 20;

export function useNotifications(params: NotificationQueryParams = {}) {
  const queryClient = useQueryClient();
  const { unreadOnly, size = DEFAULT_PAGE_SIZE } = params;
  const [page, setPage] = useState(0);

  // Reset to the first page whenever the filter or page size changes.
  useEffect(() => {
    setPage(0);
  }, [unreadOnly, size]);

  const queryParams: NotificationQueryParams = { unreadOnly, page, size };

  const notificationsQuery = useQuery({
    queryKey: [...NOTIFICATIONS_QUERY_KEY, queryParams],
    queryFn: ({ signal }) => fetchMyNotifications(queryParams, signal),
    staleTime: 15_000,
    refetchInterval: 30_000,
  });

  const unreadCountQuery = useQuery({
    queryKey: UNREAD_COUNT_QUERY_KEY,
    queryFn: ({ signal }) => fetchUnreadCount(signal),
    staleTime: 15_000,
    refetchInterval: 30_000,
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => markNotificationAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATIONS_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: UNREAD_COUNT_QUERY_KEY });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => markAllNotificationsAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATIONS_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: UNREAD_COUNT_QUERY_KEY });
    },
  });

  const totalElements = notificationsQuery.data?.totalElements ?? 0;
  const apiTotalPages = notificationsQuery.data?.totalPages;
  const totalPages = Math.max(1, apiTotalPages ?? Math.ceil(totalElements / size));

  return {
    notifications: notificationsQuery.data?.content ?? [],
    totalElements,
    totalPages,
    page,
    setPage,
    isLoading: notificationsQuery.isLoading,
    isError: notificationsQuery.isError,
    unreadCount: unreadCountQuery.data ?? 0,
    markAsRead: markReadMutation.mutate,
    markAllAsRead: markAllReadMutation.mutate,
    isMarkingAll: markAllReadMutation.isPending,
    refetch: notificationsQuery.refetch,
  };
}
