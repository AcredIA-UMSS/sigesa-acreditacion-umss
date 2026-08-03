import { useList } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type { UserAdminSummaryResponse } from '../../../../api/model/userAdminSummaryResponse';
import { getRoleLabel, isBackendRoleCode } from '../../../../lib/auth/roleLabels';

export interface UserRowViewModel {
  userId: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  roleLabel: string;
  status: string;
  canDeactivate: boolean;
}

function resolveFullName(user: UserAdminSummaryResponse): string {
  if (user.fullName?.trim()) {
    return user.fullName.trim();
  }
  const composed = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
  return composed || '—';
}

function toRow(user: UserAdminSummaryResponse): UserRowViewModel | null {
  if (!user.userId || !user.email || !user.role || !user.status) {
    return null;
  }

  return {
    userId: user.userId,
    fullName: resolveFullName(user),
    email: user.email,
    phoneNumber: user.phoneNumber?.trim() || '—',
    roleLabel: isBackendRoleCode(user.role) ? getRoleLabel(user.role) : user.role,
    status: user.status,
    canDeactivate: user.status !== 'DEACTIVATED',
  };
}

export function useUsersList() {
  const query = useList(undefined);

  const users = (query.data?.data ?? [])
    .map(toRow)
    .filter((row: UserRowViewModel | null): row is UserRowViewModel => row !== null);

  return {
    users,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
  };
}
