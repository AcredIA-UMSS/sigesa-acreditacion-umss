import { useCallback, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getListMyInAppNotificationsQueryKey,
  useListMyInAppNotifications,
  useMarkAllInAppNotificationsRead,
  useMarkInAppNotificationRead,
} from '../../../api/endpoints/notifications/notifications';
import type { InAppNotificationResponseDto } from '../../../api/model';
import { isApiError } from '../../../lib/api/apiError';
import { useAuth } from '../../../lib/auth/useAuth';
import { useQueryClient } from '@tanstack/react-query';
import type { NotificationItemView } from '../types';

const POLL_MS = 60_000;
const LIST_PARAMS = { unreadOnly: false, page: 0, size: 15 } as const;

function mapNotification(dto: InAppNotificationResponseDto): NotificationItemView | null {
  if (!dto.id) {
    return null;
  }
  return {
    id: dto.id,
    title: dto.title?.trim() || 'Notificación',
    body: dto.body?.trim() || '',
    deepLink: dto.deepLink?.trim() || null,
    read: dto.readAt != null && dto.readAt !== '',
    createdAt: dto.createdAt ?? null,
    eventType: dto.eventType ?? null,
  };
}

export function resolveDeepLinkPath(deepLink: string): string {
  try {
    if (deepLink.startsWith('http://') || deepLink.startsWith('https://')) {
      const url = new URL(deepLink);
      return `${url.pathname}${url.search}${url.hash}`;
    }
  } catch {
    // fall through to raw path
  }
  return deepLink.startsWith('/') ? deepLink : `/${deepLink}`;
}

export function useNotificationInbox() {
  const { session } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const enabled = Boolean(session?.accessToken);

  const listQuery = useListMyInAppNotifications(LIST_PARAMS, {
    query: {
      enabled,
      refetchInterval: enabled ? POLL_MS : false,
      refetchOnMount: 'always',
      staleTime: 15_000,
    },
  });

  const markReadMutation = useMarkInAppNotificationRead();
  const markAllMutation = useMarkAllInAppNotificationsRead();

  const items = useMemo(() => {
    const content = listQuery.data?.status === 200 ? listQuery.data.data.content ?? [] : [];
    return content
      .map(mapNotification)
      .filter((item): item is NotificationItemView => item != null);
  }, [listQuery.data]);

  const unreadCount = useMemo(() => items.filter((item) => !item.read).length, [items]);

  const errorMessage = useMemo(() => {
    if (!listQuery.isError) {
      return null;
    }
    return isApiError(listQuery.error)
      ? listQuery.error.message
      : 'No se pudieron cargar las notificaciones.';
  }, [listQuery.error, listQuery.isError]);

  const invalidate = useCallback(async () => {
    await queryClient.invalidateQueries({
      queryKey: getListMyInAppNotificationsQueryKey(LIST_PARAMS),
    });
  }, [queryClient]);

  const onToggle = useCallback(() => {
    setOpen((prev) => !prev);
  }, []);

  const onClose = useCallback(() => {
    setOpen(false);
  }, []);

  const onItemClick = useCallback(
    async (item: NotificationItemView) => {
      setOpen(false);
      if (!item.read) {
        try {
          await markReadMutation.mutateAsync({ id: item.id });
          await invalidate();
        } catch {
          // Still attempt deep-link navigation when present.
        }
      }
      // Empty/null deepLink (e.g. ProcessUnassigned): mark read only, no navigation.
      if (item.deepLink) {
        navigate(resolveDeepLinkPath(item.deepLink));
      }
    },
    [invalidate, markReadMutation, navigate],
  );

  const onMarkAllRead = useCallback(async () => {
    try {
      await markAllMutation.mutateAsync();
      await invalidate();
    } catch {
      // leave error state to list refetch / next poll
    }
  }, [invalidate, markAllMutation]);

  return {
    open,
    unreadCount,
    items,
    isLoading: listQuery.isLoading,
    isMarkingAll: markAllMutation.isPending,
    errorMessage,
    onToggle,
    onClose,
    onItemClick,
    onMarkAllRead,
  };
}
