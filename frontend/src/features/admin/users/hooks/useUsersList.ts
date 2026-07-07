import { useQueryClient } from '@tanstack/react-query';
import {
  getListQueryKey,
  useList,
} from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type { UserAdminSummaryResponse } from '../../../../api/model/userAdminSummaryResponse';
import { getRoleLabel, isBackendRoleCode } from '../../../../lib/auth/roleLabels';

export interface UserRowViewModel {
  userId: string;
  email: string;
  roleLabel: string;
  status: string;
  canDeactivate: boolean;
}

function toRow(user: UserAdminSummaryResponse): UserRowViewModel | null {
  if (!user.userId || !user.email || !user.role || !user.status) {
    return null;
  }

  return {
    userId: user.userId,
    email: user.email,
    roleLabel: isBackendRoleCode(user.role) ? getRoleLabel(user.role) : user.role,
    status: user.status,
    canDeactivate: user.status !== 'DEACTIVATED',
  };
}

export function useUsersList() {
  const queryClient = useQueryClient();
  const query = useList(undefined);

  const users = (query.data?.data ?? [])
    .map(toRow)
    .filter((row: UserRowViewModel | null): row is UserRowViewModel => row !== null);

  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: getListQueryKey(undefined) });
  };

  return {
    users,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    refresh,
  };
}
